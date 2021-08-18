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

import com.lmax.disruptor.RingBuffer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * This class is designed to be a TCP server which accepts a single connection from
 * a client that is sending sampa data in either DAS or DSP mode.
 * It is made to work in conjunction with the SMPTwoStreamAggregtorDecoder class.
 * The SMPTwoStreamAggregtorDecoder is, in turn, also working with SAggregator and SConsumer
 * classes.
 */
public class SReceiver extends Thread {

    /** Input data stream carrying data from the client. */
    private DataInputStream dataInputStream;

    /** ID number of the data stream. */
    private final int streamId;

    private int sampaPort;

    /** Buffer used to read a single frame of data. */
    private final ByteBuffer frameBuffer;

    /** Array wrapped by frameBuffer. */
    private final byte[] frameArray = new byte[16];

    /** Int array holding frame data in word form. */
    private final int[] data = new int[4];

    /** Type of data coming from SAMPA board. */
    private final SampaType sampaType;

    /** Object used to decode the data. */
    private final SampaDecoder sampaDecoder;

    /** Total number of frames consumed before printing stats and exiting. */
    private final int streamFrameLimit;

    /** TCP server socket. */
    private ServerSocket serverSocket;

    /** Is the incoming data format DAS? */
    private final boolean isDAS;

    /** Is the incoming data format DSP? */
    private final boolean isDSP;


    //--------------------------------
    // Disruptor stuff
    //--------------------------------

    /** Output disruptor ring buffer. */
    private final RingBuffer<SRingRawEvent> ringBuffer;

    /** Current spot in the ring from which an item was claimed. */
    private long sequenceNumber;


    /**
     * Constructor.
     *
     * @param sampaPort TCP server port.
     * @param streamId  data stream id number.
     * @param ringBuffer disruptor's ring buffer used to pass the data received here on
     *                   to an aggregator and from there it's passed to a data consumer.
     * @param streamFrameLimit total number of frames consumed before printing stats and exiting.
     * @param sampaType type of data coming over TCP client's socket.
     * @param byteSize  size in bytes of each raw event's internal buffer.
     */
    public SReceiver(int sampaPort,
                     int streamId,
                     RingBuffer<SRingRawEvent> ringBuffer,
                     int streamFrameLimit,
                     SampaType sampaType,
                     int byteSize) {

        this.sampaPort = sampaPort;
        this.ringBuffer = ringBuffer;
        this.streamId = streamId;
        this.streamFrameLimit = streamFrameLimit;
        this.sampaType = sampaType;

        frameBuffer = ByteBuffer.wrap(frameArray);
        frameBuffer.order(ByteOrder.LITTLE_ENDIAN);

        boolean verbose = false;

        if (sampaType.isDSP()) {
            sampaDecoder = new DspDecoder(verbose);
            isDSP = true;
            isDAS = false;
        }
        else {
            sampaDecoder = new DasDecoder(false, streamId, byteSize);
            isDAS = true;
            isDSP = false;
        }
    }

    /**
     * Get the next available item in ring buffer for writing data.
     *
     * @return next available item in ring buffer.
     * @throws InterruptedException if thread interrupted.
     */
    private SRingRawEvent get()  {

        sequenceNumber = ringBuffer.next();
        return ringBuffer.get(sequenceNumber);
    }


    /**
     * Place full rawEvent back into ring buffer for aggregator to receive.
     */
    private void publish() {
        ringBuffer.publish(sequenceNumber);
    }


    /**
     * Process one frame of data and place update rawEvent
     * @param rawEvent event to update (write data into it if DSP, or track frames if DAS).
     * @throws IOException   if error reading data.
     */
    public void processOneFrame(SRingRawEvent rawEvent) throws IOException {
        frameBuffer.clear();

        // clear gbt_frame: 4, 4-byte words
        dataInputStream.readFully(frameArray);


        data[3] = frameBuffer.getInt();
        data[2] = frameBuffer.getInt();
        data[1] = frameBuffer.getInt();
        data[0] = frameBuffer.getInt();

        try {
            sampaDecoder.decodeSerial(data, rawEvent);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        // Connecting to the sampa stream source
        try {
            serverSocket = new ServerSocket(sampaPort);
System.out.println("Server is listening on port " + sampaPort);
            Socket socket = serverSocket.accept();
System.out.println("SAMPA client connected");
            InputStream input = socket.getInputStream();
            dataInputStream = new DataInputStream(new BufferedInputStream(input, 65536));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        int frameCount = 0;

        try {
            do {
                // Get an empty item from ring
                SRingRawEvent rawEvent = get();
                rawEvent.reset();

                // Fill event with data until it's full or hits the frame limit
                do {
                    processOneFrame(rawEvent);
                    frameCount++;
                    
                    // In DSP mode, the rawEvent becomes full once "block" number of frames have been stored in it.
                    // In DAS mode, the decoder becomes full when it cannot hold any more raw data bytes and needs
                    // to pass them to the rawEvent.
                    // We also need to account for any frame limit specified on the command line.

                } while ( !((isDAS && sampaDecoder.isFull()) ||
                            (isDSP && rawEvent.isFull())     ||
                            ((streamFrameLimit != 0) && (frameCount >= streamFrameLimit))) );

                if (isDSP) {
                    if (rawEvent.isFull()) {
                        // Update the block number since the event becomes full once
                        // a complete block of data has been written into it.
                        rawEvent.setBlockNumber(sampaDecoder.incrementBlockCount());
                        //int blockCount = sampaDecoder.getBlockCount();
                        //if (streamId == 2 && (blockCount % 1000 == 0)) System.out.println("Raw event full, set block num to " + blockCount);
                    }
                }
                else {
                    ((DasDecoder) sampaDecoder).transferData(rawEvent);
                    //if (streamId == 2) System.out.println("Transferred str2 at framecount = " + frameCount);
                }

                // Print out
//                rawEvent.printData(System.out, streamId, true);
//                rawEvent.calculateStats();
//                rawEvent.printStats(System.out, false);

                // Make the buffer available for consumers
                publish();

                // Loop until we run into our given limit of frames
            } while ((streamFrameLimit == 0) || (frameCount < streamFrameLimit));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        exit();
    }



    public void exit() {
        try {
            dataInputStream.close();
            serverSocket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        this.interrupt();
    }
}