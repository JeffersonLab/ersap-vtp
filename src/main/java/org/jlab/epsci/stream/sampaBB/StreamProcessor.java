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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * <p>This class creates a TCP server which accepts a client's socket connection.
 * The client sends DSP or DAS data over the socket. This class is the data's
 * final destination and all is does is print out the data.</p>
 * In the case of DSP format data, it's printed out in blocks.
 */
public class StreamProcessor {

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


    /**
     * Constructor.
     * @param sampaPort TCP server port.
     * @param streamId  data stream id number.
     * @param sampaType type of data coming over socket.
     * @throws IOException if error communicating over TCP.
     */
    public StreamProcessor(int sampaPort, int streamId, SampaType sampaType) throws IOException {
        this.streamId  = streamId;
        this.sampaType = sampaType;

        frameBuffer = ByteBuffer.wrap(frameArray);
        frameBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // What kind of data are we decoding?
        if (sampaType.isDSP()) {
            sampaDecoder = new DspDecoder();
        }
        else {
            sampaDecoder = new DasDecoder();
        }

        // Allow sampa data source to connect to us
        ServerSocket serverSocket = new ServerSocket(sampaPort);
        System.out.println("Server is listening on port " + sampaPort);
        Socket socket = serverSocket.accept();
        System.out.println("SAMPA client connected");
        InputStream input = socket.getInputStream();
        dataInputStream = new DataInputStream(new BufferedInputStream(input, 65536));
    }

    public void processOneFrame(SRingRawEvent rawEvent) {
        frameBuffer.clear();

        try {
            // Read a frame: 4, 32-bit words
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

    public static void main(String[] args) {
        int port1 = Integer.parseInt(args[0]);
        int streamId = Integer.parseInt(args[1]);
        int streamFrameLimit = Integer.parseInt(args[2]);

        SampaType sampaType = SampaType.DSP;

        StreamProcessor s = null;
        try {
            s = new StreamProcessor(port1, streamId, sampaType);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // All data storage is now in the raw event object
        SRingRawEvent rawEvent = new SRingRawEvent(sampaType);
        int frameCount = 0;

        try {
            do {
                rawEvent.reset();

                // Fill event with data until it's full or hits the frame limit
                do {
                    s.processOneFrame(rawEvent);
                    frameCount++;
                    // Loop until event is full or we run into our given limit of frames
                } while (!(rawEvent.isFull() || (frameCount == streamFrameLimit)));

                if (sampaType.isDSP() && rawEvent.isFull()) {
                    // Update the block number since the event becomes full once
                    // a complete block of data has been written into it.
                    rawEvent.setBlockNumber(s.sampaDecoder.incrementBlockCount());
                }

                // Print out
                rawEvent.printData(System.out, streamId, true);
                rawEvent.calculateStats();
                rawEvent.printStats(System.out, false);

                // Loop until we run into our given limit of frames
            } while (frameCount < streamFrameLimit);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("   num stream = " + streamId);
        if (s.sampaType.isDSP()) {
            System.out.println("   block count = " + s.sampaDecoder.getBlockCount());
        }
    }

}