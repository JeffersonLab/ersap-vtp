package org.jlab.epsci.stream.vtp.evio;

/**
 * Copyright (c) 2021, Jefferson Science Associates, all rights reserved.
 * See LICENSE.txt file.
 * Thomas Jefferson National Accelerator Facility
 * Experimental Physics Software and Computing Infrastructure Group
 * 12000, Jefferson Ave, Newport News, VA 23606
 * Phone : (757)-269-7100
 *
 * @author timmer on 1/18/22
 * @project ersap-vtp
 */

import org.jlab.coda.jevio.*;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;


/**
 * <p>
 * Class for reading data buffers containing fADC data,
 * packaging each buffer containing that data as a single evio event,
 * and then writing each event into an evio format file.
 * </p>
 * An example of a file name is "/data/sro/sro_file.evio.run%05d.split%05d".
 * The first %05d will be substituted by the run number.
 * The second %05d will be substituted by the internally generated split number.
 * In both cases, the numbers are forced to be exactly 5 digits,
 * with prepended zeros if necessary.
 */
public class CodaSroToFile implements AutoCloseable {

    static final int MAXCHAN = 16;
    static final int SLOT_LEN = 12;    /* used for events selection;
                                        for example 9 means at least 1 hit, 10-2hits, 11-3hits, etc */

    /** Byte size of internal buffers to hold a single channel of adc or tdc data. */
    static final int MAXBUF = 4000;
    /** Byte size of internal buffer to hold evio data. */
    static final int MAXEVIOBUF = 4000000;

    static final int runNumber = 0;

    static int iev;
    static final int MYCRATE = 21; // halldfdc1 crate id
    static final int MYSLOT = 3;   // FADC slot in halldfdc1
    static final int MAGIC_NUMBER = 0xC0DA2019;

    private boolean verbose;

    /** Byte order of output file, defaulting to little endian. */
    private final ByteOrder outputOrder;

    /** Name of output file. */
    private final String outputFilename;

    /** Object to write buffers to evio file. */
    private final EventWriterUnsync writer;

    /** Object to build an event. */
    private final CompactEventBuilder evBuilder;


    /**
     * Constructor with little endian file output and MAXEVIOBUF sized
     * internal buffer.
     *
     * @param filename output file name.
     * @throws EvioException maxEventBytes (buffer size) is too small or
     *                       file cannot be created.
     */
    public CodaSroToFile(String filename) throws EvioException {
        this(filename, ByteOrder.LITTLE_ENDIAN, MAXEVIOBUF);
    }


    /**
     * Constructor.
     *
     * @param filename output file name.
     * @param order byte order of output file.
     * @param maxEventBytes estimated maximum byte size of 1 event of data.
     * @throws EvioException maxEventBytes (buffer size) is too small or
     *                       file cannot be created.
     */
    public CodaSroToFile(String filename, ByteOrder order, int maxEventBytes) throws EvioException {
        outputFilename = filename;
        outputOrder = order;
        writer = new EventWriterUnsync(outputFilename, false, order);
        evBuilder = new CompactEventBuilder(maxEventBytes, order);
    }


    /** Close file writer. */
    public void close() {
        if (writer != null) {
            writer.close();
        }
    }


    /**
     * Set debug printout.
     * @param verbose if true, print debug info.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }


    /**
     * Process one buffer of data by parsing it into
     * multiple evio events and writing each event to file.
     *
     * @param dabuf buffer containing raw fADC data.
     * @throws EvioException internal format errors, writer is closed, file issues.
     * @throws IOException if unable to write to file.
     * @throws BufferOverflowException if too little data.
     */
    public void fadcToEvio(byte[] dabuf) throws EvioException, IOException, BufferOverflowException {

        Objects.requireNonNull(dabuf);
        if (dabuf.length < 52) {
            throw new BufferUnderflowException();
        }

        boolean bank_opened;
        int  time_min, val, i, j, jj, nn, slot_len, type=0, rocid, slot;
        long frame_time_ns, timestamp = 0L;

        int fragtag = MYCRATE;
        int fragnum = -1;
        int banknum = 0;
        int banktag = 0xe103; // FADC250 Pulse Integral Data (mode 3)
        String fmt = "c,i,l,N(c,N(s,i))"; // slot,event#,timestamp,Nchannels(channel,Nhits(tdc,adc))

        int nhits;
        int[] nhit = new int[MAXCHAN];
        int[][] adc = new int[MAXCHAN][MAXBUF];
        int[][] tdc = new int[MAXCHAN][MAXBUF];
        CODA_SRO_Header codaSroHeader = new CODA_SRO_Header();

        // Initialize
        nhits = 0;
        Arrays.fill(nhit,0);

        //------------------------
        // Find endianess of data
        //------------------------
        ByteOrder order = ByteOrder.LITTLE_ENDIAN;
        int magic = ByteDataTransformer.toInt(dabuf, order, CODA_SRO_Header.getMagicOffset());
        if (magic != MAGIC_NUMBER) {
            order = ByteOrder.BIG_ENDIAN;

            // Reread magic number to make sure things are OK
            magic = Integer.reverseBytes(magic);
            if (magic != MAGIC_NUMBER) {
                System.out.println("ERROR reread magic # (" + magic + ") & still not right");
                throw new EvioException("bad magic #");
            }
        }
        //-----------------------

        int wordLen = ByteDataTransformer.toInt(dabuf, order, 0);
        if (verbose) System.out.println("eventbuilder: len=" + wordLen);

        int wordPos = 1;

        CompositeData[] cData = new CompositeData[1];
        CompositeData.Data fadcData = new CompositeData.Data();;

        while (wordPos < wordLen) {

            //**********************************************
            // first 13 words are a 'CODA_SRO_Header', decode it
            codaSroHeader.read(dabuf, 4*wordPos, order);
            if (codaSroHeader.magic != 0xC0DA2019) {
                System.out.println("eventbuilder1: ERROR: codasro_msg.magic != 0xC0DA2019, received 0x" +
                        Integer.toHexString(codaSroHeader.magic) + " - exiting thread\n");
                throw new EvioException("bad magic number in header, 0x" + Integer.toHexString(codaSroHeader.magic));
            }

            frame_time_ns = codaSroHeader.record_counter * 65536L;
            wordPos += 13;

            // the number of words in following data
            slot_len = codaSroHeader.payload_length / 4;
            if (slot_len > SLOT_LEN) {
                System.out.println("\neventbuilder1: slot_len=" + slot_len + " words (wordPos=" + wordPos + ")");
            }

            bank_opened = false;
            for (jj=0; jj < slot_len; jj++) {
                if (wordPos >= wordLen) {
                    // should never happens, just in case ...
                    System.out.println("eventbuilder1: ERROR: exit for(jj) loop prematurely at jj=" + jj +
                            " (slot_len=" + slot_len + "): wordPos=" + wordPos + " >= wordLen=" + wordLen);
                    throw new EvioException("exit loop prematurely");
                }

                val = ByteDataTransformer.toInt(dabuf, order, 4*wordPos);
                wordPos++;

                // extract FADC data
                if ((val & 0x80000000) != 0) {
                    type  = (val >> 15) & 0xFFFF;
                    rocid = (val >> 8)  & 0x007F;
                    slot  = (val >> 0)  & 0x001F;

                    if ((slot_len > SLOT_LEN) && (type > 0)) {
                        timestamp = frame_time_ns;

                        if (verbose) {
                            System.out.println("  [" + jj + "] type=" + type + " rocid=" + rocid + " slot=" + slot +
                                    " timestamp=" + timestamp + " (wordPos=" + wordPos + ")");
                            System.out.println("Opening bank ...");
                        }

                        if (bank_opened) {
                            if (verbose) System.out.println("ERROR: bank already opened !!!");
                        }
                        bank_opened = true;

                        // Clear out old data if any
                        evBuilder.reset();

                        // Evio event
                        evBuilder.openBank(129, 0xCC, DataType.BANK);

                        // Bank of all banks from 1 vme crate
                        evBuilder.openBank(fragtag, fragnum, DataType.BANK);

                        // Bank of VME data in composite form
                        evBuilder.openBank(banktag, banknum, DataType.COMPOSITE);
                    }
                }
                // FADC hit type
                else if (type == 0x0001) {
                    int q  =  (val >>  0) & 0x1FFF;
                    int ch =  (val >> 13) & 0x000F;
                    int t  = ((val >> 17) & 0x3FFF) * 4;
                    long hit_time = frame_time_ns + t;

                    if ((slot_len > SLOT_LEN) && (type > 0)) {
                        if (verbose) System.out.println("    [" + jj + "] q=" + q + " ch=" + ch + " t=" + t + " hit_time=" + hit_time);

                        nn = nhit[ch];
                        adc[ch][nn] = q;
                        tdc[ch][nn] = t;
                        nhit[ch]++;
                        nhits++;
                    }
                }
                else {
                    // unknown hit type
                    //printf("      [%3d] ERROR: unknown hit type=0x%08x, val=0x%08x (wordPos=%d)\n",jj,type,val,wordPos);
                }
            } /*for(jj=0; jj<slot_len; jj++)*/



            if (nhits > 0) {
                if (verbose) System.out.println("\nData Buf processed");
                time_min = 10000000;

                for (i=0; i < MAXCHAN; i++) {
                    if (nhit[i] > 0) {
                        if (verbose) System.out.print("BEFOR: CHANNEL " + i + ", nhit=" + nhit[i] + ":");
                        for (j=0; j < nhit[i]; j++) {
                            if (time_min > tdc[i][j])  {
                                time_min = tdc[i][j];
                            }
                            if (verbose) System.out.print(" (a=" + adc[i][j] + ", t=" + tdc[i][j]);
                        }
                        if (verbose) System.out.println();
                    }
                }

                if (verbose) System.out.println("time_min=" + time_min);
                if (verbose) System.out.println("old timestamp=" + timestamp);
                timestamp = timestamp + time_min;
                if (verbose) System.out.println("new timestamp=" + timestamp);

                for (i=0; i < MAXCHAN; i++) {
                    if (nhit[i] > 0) {
                        if (verbose) System.out.print("AFTER: CHANNEL " + i + ", nhit=" + nhit[i] + ":");
                        for (j=0; j<nhit[i]; j++) {
                            tdc[i][j] = tdc[i][j] - time_min;
                            if (verbose) System.out.print(" (a=" + adc[i][j] + ", t=" + tdc[i][j]);
                        }
                        if (verbose) System.out.println();
                    }
                }

//                String fmt = "c,i,l,N(c,N(s,i))"; /*slot,event#,timestamp,Nchannels(channel,Nhits(tdc,adc))*/
//                c = unsigned char,
//                i = unsigned int,
//                l = unsigned long,
//                s = unisgned short
//                N = unsigned int

                // Clear CompositeData object in prep for filling
                fadcData.clear();

                // Fill bank
                fadcData.addUchar((byte)MYSLOT);    // PUT8(MYSLOT);
                fadcData.addUint(iev++);            // PUT32(iev++);
                fadcData.addUlong(timestamp);       // PUT64(timestamp);
                fadcData.addN(nhits);               // PUT32(nhits);
                // Original line below replaced by one above!
                // fadcData.addN(0);                // PUT32(0);

                for(i=0; i < MAXCHAN; i++) {
                    if (nhit[i] > 0) {
                        fadcData.addUchar((byte)i);  // PUT8(i);
                        fadcData.addUint(nhit[i]);   // PUT32(nhit[i]);
                        for (j=0; j < nhit[i]; j++) {
                            fadcData.addUshort((short)tdc[i][j]);  // PUT16(tdc[i][j]);
                            fadcData.addUint(adc[i][j]);           // PUT32(adc[i][j]);
                        }
                    }
                }

                // Create CompositeData object
                try {
                    if (cData[0] != null) {
                        // Save on creating some objects by skipping constructor and doing a reset
                        cData[0].resetData(fadcData);
                    }
                    else {
                        cData[0] = new CompositeData(fmt, 0, fadcData, 0, 0);
                    }

                    evBuilder.addCompositeData(cData);
                }
                catch (EvioException e) {
                    e.printStackTrace();
                }
            }

            nhits = 0;
            Arrays.fill(nhit,0);

            if (bank_opened) {
                if (verbose) System.out.println("Closing bank ...");

                // evio
                evBuilder.closeAll();
                writer.writeEvent(evBuilder.getBuffer());
            }
        } /*while(wordPos < wordLen)*/

    }

}