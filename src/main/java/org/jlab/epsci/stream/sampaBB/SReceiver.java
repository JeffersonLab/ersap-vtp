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
    private final DataInputStream dataInputStream;

    /** ID number of the data stream. */
    private final int streamId;

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
    private final ServerSocket serverSocket;

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
     * @throws IOException if error communicating over TCP.
     */
    public SReceiver(int sampaPort,
                     int streamId,
                     RingBuffer<SRingRawEvent> ringBuffer,
                     int streamFrameLimit,
                     SampaType sampaType) throws IOException {
        this.ringBuffer = ringBuffer;
        this.streamId = streamId;
        this.streamFrameLimit = streamFrameLimit;
        this.sampaType = sampaType;

        frameBuffer = ByteBuffer.wrap(frameArray);
        frameBuffer.order(ByteOrder.LITTLE_ENDIAN);

        if (sampaType.isDSP()) {
            sampaDecoder = new DspDecoder();
        }
        else {
            sampaDecoder = new DasDecoder();
        }

        // Connecting to the sampa stream source
        serverSocket = new ServerSocket(sampaPort);
        System.out.println("Server is listening on port " + sampaPort);
        Socket socket = serverSocket.accept();
        System.out.println("SAMPA client connected");
        InputStream input = socket.getInputStream();
        dataInputStream = new DataInputStream(new BufferedInputStream(input, 65536));
    }

    /**
     * Get the next available item in ring buffer for writing data.
     *
     * @return next available item in ring buffer.
     * @throws InterruptedException if thread interrupted.
     */
    private SRingRawEvent get() throws InterruptedException {

        sequenceNumber = ringBuffer.next();
        return ringBuffer.get(sequenceNumber);
    }

    private void publish() {
        ringBuffer.publish(sequenceNumber);
    }

    public void processOneFrame(SRingRawEvent rawEvent) {
        frameBuffer.clear();

        try {
            // clear gbt_frame: 4 4-byte, 32-bit words
            dataInputStream.readFully(frameArray);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

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
                    // Loop until event is full or we run into our given limit of frames
                } while (!(rawEvent.isFull() || (frameCount == streamFrameLimit)));

                if (sampaType.isDSP() && rawEvent.isFull()) {
                    // Update the block number since the event becomes full once
                    // a complete block of data has been written into it.
                    rawEvent.setBlockNumber(sampaDecoder.incrementBlockCount());
                }

                // Print out
                rawEvent.printData(System.out, streamId, true);
                rawEvent.calculateStats();
                rawEvent.printStats(System.out, false);

                // Make the buffer available for consumers
                publish();

                // Loop until we run into our given limit of frames
            } while (frameCount < streamFrameLimit);
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