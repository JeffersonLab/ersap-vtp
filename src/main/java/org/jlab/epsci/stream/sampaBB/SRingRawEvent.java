/*
 * Copyright (c) 2021, Jefferson Science Associates, all rights reserved.
 * See LICENSE.txt file.
 *
 * Thomas Jefferson National Accelerator Facility
 * Experimental Physics Software and Computing Infrastructure Group
 *
 * 12000, Jefferson Ave, Newport News, VA 23606
 * Phone : (757)-269-7100
 */

package org.jlab.epsci.stream.sampaBB;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * <p>This class implements objects contained in a disruptor's ring buffer.
 * It's designed to stored parsed data from a SAMPA board used in CODA
 * Data Acquisition.</p>
 *
 * <p><b>We need to allocate enough memory to start. The question is, how much do we need?</b></p>
 * <p>For DAS mode:<br>
 * There are 80 channels worth of data. Each frame gets parsed into 20 bytes of data.
 * In the DasDecoder, 5 ByteBuffers each contain 4 bytes. So, if we set a max of 262144
 * frames to store, then we're using a total of 5,242,880 bytes (1,048,576 bytes in each of 5 BBs).
 * If this memory is divided into 80 channels, each channel has 65536 bytes of data.
 * This is assuming the 10 bit integer ADC values get stored as shorts in the local BBs.
 * This form of data storage is much, much more efficient than the DSP mode which blows
 * up the stored data by over a factor of 3.2.
 * One complicating factor is that occasionally the data needs to be synced and there
 * may be mismatches, requiring differing amounts of data to be stored in different channels.
 * To accommodate this, make sure buffer sizes are larger than the bare minimum.</p>
 *
 * <p>For DSP mode:
 * According to Ed,
 * "Currently the SAMPA chips are programmed to transmit non-empty packet data every 1000 ADC samples.
 * The period is called the sample window and equates to 50 us when sampling at 20 MHz.
 * This corresponds to 2000 frames of the 4.8 Gb/s data stream. We define a BLOCK of packets
 * to be the set of all packets collected during N_BLOCK consecutive sampling windows.
 * N_BLOCK is programmable (see below for current val). A BLOCK HEADER is written to a file followed
 * by all the packet data of the BLOCK. For N_BOCK = 10, the BLOCK represents 500 us of time,
 * so the file written is time-ordered to this level".</p>
 *
 * <p>What does that mean for memory? Currently:<br>
 * <code>DspDecoder.N_BLOCK = 1;
 * DspDecoder.frames_in_block = 2000 * N_BLOCK = 2000 frames/block;</code>
 * It's not possible to do an exact calculation because of zero suppression but
 * 2000 frames * 16 bytes/frame = 32kBytes. This gets expanded by a factor of ~3.2 because of
 * the way the data is repackaged (10 bits -> 32 bits). Thus, we currently need over
 * 102KB so we'll just say 131072 bytes for each 2000 frames.</p>
 *
 * <p>Don't worry because in the DspDecoder, if more memory is needed, the individual
 * ByteBuffer being used is expanded to accommodate. So no OutOfMemoryExceptions
 * will be thrown. This will initially generate garbage, but that will disappear
 * once a size is reached that will hold everything.</p>
 *
 * <p>Finally, let's just say each array = 131072 bytes. DSP mode uses 28, DAS mode uses 80 of them.
 * If 16 of these exist, 131072*80*16 = 167MB total memory used in ring buffer.
 * 131072 * 80 &lt; 11 MB total allocated in this object.
 * If this event is used for aggregating, double the memory numbers.</p>
*/
public class SRingRawEvent {

    /** Block number (DSP mode only). */
    private int blockNumber;

    /** Keep track of valid entries in localData. */
    private int validChannels;

    /** Data type. */
    private final SampaType sampaType;

    /** Number of frames from which stored data has been taken. */
    private int framesStored;

    /** Number of channels (and buffers in localData). */
    private int channelCount;

    /**
     * One ByteBuffer for each of 80 channels in DAS mode, 28 available in DSP mode.
     * In the DAS mode, each contained ADC value is stored in a short.
     */
    private final ByteBuffer[] localData;

    /**
     * <p>In DSP mode this is the BX count or bunch-crossing counter (also stored
     * in the first header word of each packet) which indicates
     * the time associated with a packet. Since all packets in one block come from
     * the same time, this single value applies to all packets contained in the block.</p>
     *
     * <p>In DAS mode no reliable time is available in the data.</p>
     */
    private int time;

    /** Mean ADC values for each channel (DAS mode only). */
    private double[] mean;

    /** Std. dev. of ADC values for each channel (DAS mode only). */
    private double[] sdv;



    /**
     * Constructor. Internal buffers are set to 131072 bytes each.
     * The number of buffers is half that being used for aggregation.
     * @param type the type of data being stored.
     */
    public SRingRawEvent(SampaType type) {this(type, 131072, false);}


    /**
     * Constructor.
     * @param type the type of data being stored.
     * @param byteSize number of bytes in each internal buffer.
     * @param forAggregation if true, this is used to hold aggregated data -
     *                       all 160 channels of a SAMPA board. Or, in other words,
     *                       it needs hold 2x the data coming from a single stream.
     */
    public SRingRawEvent(SampaType type, int byteSize, boolean forAggregation) {
        sampaType = type;

        // If aggregating we need double the channels
        int factor = 1;
        if (forAggregation) factor = 2;

       channelCount = 28*factor;

        if (sampaType.isDAS()) {
            channelCount = 80*factor;
            mean = new double[channelCount];
            sdv  = new double[channelCount];
        }

        localData = new ByteBuffer[channelCount];

        for (int i=0; i < channelCount; i++) {
            localData[i] = ByteBuffer.allocate(byteSize);
        }
    }


    /**
     * Expand a particular ByteBuffer, preserving existing data.
     *
     * @param index index into localData array of ByteBuffers.
     * @param size number of bytes in new buffer.
     * @return buffer of increased size (same buffer if size is &lt;= current size).
     */
    ByteBuffer expandBuffer(int index, int size) {
        ByteBuffer temp = localData[index];
        if (size <= temp.capacity()) {
            return temp;
        }
        // Expand BB by 25% over necessary amount to reduce future reallocations
        size = 5 * size / 4;
        localData[index] = ByteBuffer.allocate(size);
        // Copy over existing data, pos = 0 to limit
        System.arraycopy(temp.array(), 0, localData[index].array(), 0, temp.limit());
        return localData[index];
    }


    /**
     * Get the number of channels and therefore buffers.
     * @return number of channels and therefore buffers.
     */
    public int getChannelCount() {return channelCount;}

    /**
     * Get the DSP mode bunch crossing (time) for this block.
     * @return DSP mode bunch crossing (time) for this block.
     */
    public int getTime() {return time;}

    /**
     * Set the DSP mode bunch crossing (time) for this block.
     * @param time DSP mode bunch crossing (time) for this block.
     */
    public void setTime(int time) {this.time = time;}

    /**
     * Get the type of sampa data contained in the data buffers.
     * @return type of sampa data contained in the data buffers.
     */
    public SampaType getSampaType() {return sampaType;}

    /**
     * Get the block number.
     * @return block number.
     */
    public int getBlockNumber() {return blockNumber;}

    /**
     * Set the block number.
     * @param blockNumber block number.
     */
    public void setBlockNumber(int blockNumber) {this.blockNumber = blockNumber;}

    /**
     * Get the number of frames from which stored data has been taken.
     * @return number of frames from which stored data has been taken.
     */
    public long getFramesStored() {return framesStored;}

    /** Increment the number of frames stored by one. */
    public void incrementFramesStored() {framesStored++;}

    /** Clear the number of frames stored. */
    public void clearFramesStored() {framesStored = 0;}

    /**
     * Is this event full of data?
     * For DSP, is the number of frames stored exactly enough to make
     * one full block {@link DspDecoder#getFramesInBlock()}?
     * For DAS, is the number of frames at or over the limit set in
     * {@link DasDecoder#getMaxFramesStored()}?
     *
     * @return true if number of frames stored is at or over the acceptable limit.
     */
    public boolean isFull() {
        if (sampaType.isDSP()) {
            return framesStored >= DspDecoder.getFramesInBlock();
        }
        else {
            return framesStored >= DasDecoder.getMaxFramesStored();
        }
    }

    /**
     * Get the internal array of ByteBuffers.
     * @return internal array of ByteBuffers.
     */
    public ByteBuffer[] getData() {return localData;}

    /**
     * Get the ByteBuffer associated with a given eLink (DSP mode) or channel (DAS mode).
     * @param index  for DSP mode, it's the electronic link (eLink) over which this data came (0-27).
     *               For DAS mode, it's the channel number from which this data came (0-79).
     * @return ByteBuffer to store data in.
     */
    public ByteBuffer getBuffer(int index) {return localData[index];}

    /**
     * <p>Copy the data of an array of ByteBuffers into an internal array of the same.
     * Only up to the internal array capacity number of buffers are copied.</p>
     *
     * This method assumes that the data arg has contiguous non-null data entries.
     *
     * @param data array of ByteBuffers containing data.
     *             All elements at and after a null buffer are ignored.
     */
    public void setData(ByteBuffer[] data) {
        validChannels = 0;
        int dataEntries = Math.min(data.length, localData.length);
        for (int i = 0; i < dataEntries; i++) {
            // Ignore everything at and after a null buf
            if (data[i] == null) {
                System.out.println("SRingRawEvent: Error, trying to add null buffer");
                break;
            }

            // Need to actually copy the data here since the data in the arg
            // will change as soon as it reads more from the source.
            // This should be much faster than using Vectors or ArrayLists element by element
            System.arraycopy(data[i].array(), 0, localData[i].array(), 0, data[i].remaining());
            validChannels++;
        }
    }


    /**
     * Copy data from ByteBuffer into the internal array of such buffers.
     * Excess elements (when internal array is full) of the input array are ignored.
     *
     * @param data array of ByteBuffers containing data.
     *             All elements at and after a null buffer are ignored.
     */
    public void addData(ByteBuffer[] data) {
        if (validChannels >= channelCount) {
            System.out.println("SRingRawEvent: Error, RingBuffer data array limit reached.");
        }
        else {
            int dataEntries = Math.min(data.length, (channelCount - validChannels));
            for (int i = 0; i < dataEntries; i++) {
                // Ignore everything at and after a null buf
                if (data[i] == null) {
                    System.out.println("SRingRawEvent: Error, trying to add null buffer");
                    break;
                }

                System.arraycopy(data[i].array(), 0,
                                 localData[validChannels].array(), 0,
                                 data[i].remaining());
                validChannels++;
            }
        }
    }

    /** Clear the buffer stored internally. */
    public void reset() {
        time = 0;
        blockNumber = 0;
        framesStored = 0;
        validChannels = 0;

        // Not necessary to clear mean & sdv arrays as
        // calling calculateStats() will overwrite everything

        for (ByteBuffer buf : localData) {
            if (buf != null) {
                buf.clear();
            }
        }
    }


    /**
     * <p>Write data values to the output stream.</p>
     *
     * If this contains DAS data, then write ADC values to the output stream.
     * If this contains DSP data, write out values up to one whole <b>BLOCK</b>.
     * Note, even if specifying hex output, the values following
     * "eLink" and "num data" are always decimal in DSP mode.
     *
     * @param out output stream to write data to.
     * @param streamId id number of data stream.
     * @param hex if true, output values are in hexadecimal, else in decimal.
     */
    public void printData(OutputStream out, int streamId, boolean hex) {
        if (sampaType.isDAS()) {
            //printDataDAS(out, hex);
            //printDataDASChannelFirst(out, hex);
            printDataDASChannel(out, hex, 0);
            //printDataDASChannel(out, hex, 15);
            //printDataDASChannel(out, hex, 1);
        }
        else {
            printDataDSP(out, streamId, hex);
        }
    }


    /**
     * Write data values to output stream.
     * This routine only prints out values for one whole <b>BLOCK</b>.
     * If that many frames have not been parsed yet, continue to read
     * and parse data until it becomes true.
     * Note, even if specifying hex output, the values following
     * "eLink" and "num data" are always decimal.
     *
     * @param out output stream to write data to.
     * @param streamId id number of data stream.
     * @param hex if true, output values are in hexadecimal, else in decimal.
     */
    private void printDataDSP(OutputStream out, int streamId, boolean hex) {

        boolean autoFlush = true;
        PrintWriter writer = new PrintWriter(out, autoFlush, StandardCharsets.US_ASCII);

        // write block header
        int block_header = 0x20000000 | ((0xF & streamId) << 26) | (0x3FFFFFF & blockNumber);
        if (hex) {
            writer.println(Integer.toHexString(block_header));
        }
        else {
            writer.println(block_header);
        }

        // write vector data for block to output stream
        for (int jj = 0; jj < 28; jj++) {
            int numData = localData[jj].limit()/4;
            writer.print("  eLink = ");
            writer.print(jj);
            writer.print("   num data = ");
            writer.println(numData);

            if (numData > 0) {
                for (int ii = 0; ii < numData; ii++) {
                    int data = localData[jj].getInt(ii);
                    if (hex) {
                        writer.printf("%3x", data);
                    }
                    else {
                        writer.printf("%4d", data);
                    }

                    if ((ii + 1) % 10 == 0) {
                        writer.println();
                    }
                    else {
                        writer.print(" ");
                    }
                }
                writer.println();
            }
            else {
                writer.println();
            }
        }
        //writer.flush();
    }


    /**
     * Write ADC values to output stream.
     *
     * @param out output stream to write data to.
     * @param hex if true, output values are in hexadecimal, else in decimal.
     */
    private void printDataDAS(OutputStream out, boolean hex) {
        boolean autoFlush = true;
        PrintWriter writer = new PrintWriter(out, autoFlush, StandardCharsets.US_ASCII);

        // TODO: The amount of data in each channel can differ depending on the misalignment of
        // TODO: values found during the sync process. They can be off by 1 ADC value.

        // How much data do we have?
        int sampleLimit = localData[0].limit()/2;

        for (int sample = 0; sample < sampleLimit; sample++) {
            for (int channel = 0; channel < validChannels; channel++) {
                if (hex) {
                    writer.printf("%3x", localData[channel].getShort(sample));
                }
                else {
                    writer.printf("%4d", localData[channel].getShort(sample));
                }
                if ((channel+1) % 10 == 0) {
                    writer.println();
                }
                else {
                    writer.print(" ");
                }
            }
            writer.println();
        }
        //writer.flush();
    }

    /**
     * Write ADC values to output stream.
     *
     * @param out output stream to write data to.
     * @param hex if true, output values are in hexadecimal, else in decimal.
     */
    private void printDataDASChannelFirst(OutputStream out, boolean hex) {
        boolean autoFlush = true;
        PrintWriter writer = new PrintWriter(out, autoFlush, StandardCharsets.US_ASCII);


        for (int channel = 0; channel < validChannels; channel++) {
            writer.print("Channel #");
            writer.println(channel);

            // How much data do we have?
            int sampleLimit = localData[channel].limit()/2;

            for (int sample = 0; sample < sampleLimit; sample++) {
                if (hex) {
                    writer.printf("%3x", localData[channel].getShort(sample));
                }
                else {
                    writer.printf("%4d", localData[channel].getShort(sample));
                }

                if ((sample+1) % 10 == 0) {
                    writer.println();
                }
                else {
                    writer.print(" ");
                }
            }

            writer.println();
            writer.println();
        }
        //writer.flush();
    }

    /**
     * Write ADC values to output stream.
     *
     * @param out output stream to write data to.
     * @param hex if true, output values are in hexadecimal, else in decimal.
     */
    private void printDataDASChannel(OutputStream out, boolean hex, int channel) {
        boolean autoFlush = true;
        PrintWriter writer = new PrintWriter(out, autoFlush, StandardCharsets.US_ASCII);

             if (channel > validChannels - 1) {
                 channel =  validChannels - 1;
             }

            writer.print("Channel #");
            writer.println(channel);

            // How much data do we have?
            int sampleLimit = localData[channel].limit()/2;

            for (int sample = 0; sample < sampleLimit; sample++) {
                if (hex) {
                    writer.printf("%3x", localData[channel].getShort(sample));
                }
                else {
                    writer.printf("%4d", localData[channel].getShort(sample));
                }

                if ((sample+1) % 10 == 0) {
                    writer.println();
                }
                else {
                    writer.print(" ");
                }
            }

            writer.println();
            writer.println();
        //writer.flush();
    }


    /** Calculate statistics (DAS mode only). */
    public void calculateStats() {
        if (sampaType.isDSP()) {
            return;
        }

        double m, M2, variance, delta, dataPt;

        // How much data do we have?
        int sampleLimit = localData[0].limit()/2;

        for (int channel = 0; channel < 80; channel++) {
            m = 0;
            M2 = 0;
            variance = 0;

            for (int sample = 0; sample < sampleLimit; sample++) {
                dataPt = localData[channel].getShort(sample);
                delta = dataPt - m;
                m  += delta / (sample + 1);
                M2 += delta * (dataPt - m);
                variance = M2 / (sample + 1);
            };

            mean[channel] = m;
            sdv[channel]  = Math.sqrt(variance);
        };
    }


    /**
     * Write statistics to output stream (DAS mode only).
     * @param out output stream
     * @param json true if json format desired
     */
    public void printStats(OutputStream out, boolean json) {
        if (sampaType.isDSP()) {
            return;
        }

        boolean autoFlush = true;
        PrintWriter writer = new PrintWriter(out, autoFlush, StandardCharsets.US_ASCII);

        writer.print(5);
        writer.write((json ? "{\n" : ""));
        writer.write((json ? "\"input_name\" : " : "Input name : "));
        writer.write("\"");
        writer.write("streaming");
        writer.write("\"");
        writer.write((json ? ",\n" : "\n"));

        if (json) {
            writer.write("\"mean\": [");
            for (int channel = 0; channel < 80; channel++) {
                writer.printf("%8.4f", mean[channel]);
                writer.write((channel == 79 ? "]" : ", "));
            }

            writer.write(",\n");
            writer.write("\"stdev\": [");

            for (int channel = 0; channel < 80; channel++) {
                writer.printf("%6.4f", sdv[channel]);
                writer.write((channel == 79 ? "]" : ", "));
            }
        }
        else {
            for (int channel = 0; channel < 80; channel++) {
                writer.write("[ CHA ");
                writer.printf("%2d] : ", channel);
                writer.printf("%8.4f   ", mean[channel]);
                writer.printf("%6.4f\n", sdv[channel]);

            };
        };

        writer.write((json ? "\n}" : ""));
        writer.write("\n");
    }

}
