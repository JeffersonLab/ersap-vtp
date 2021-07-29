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
 * This class is designed to take the place of the SReceiver and receive all data coming
 * from the SAMPA board. It thens just dumps it, but prints out how many frames were read.
 * Useful only for testing the data flow from the board to the first receiving component.
 * Really designed for DAS mode.
 *
 * @author timmer
 */
public class ReceiveAndDumper extends Thread {

    /** Input data stream carrying data from the client. */
    private DataInputStream dataInputStream;

    /** ID number of the data stream. */
    private final int streamId;

    private final int sampaPort;

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


    /**
     * Constructor.
     *
     * @param sampaPort TCP server port.
     * @param streamId  data stream id number.
     * @param streamFrameLimit total number of frames consumed before printing stats and exiting.
     * @param sampaType type of data coming over TCP client's socket.
     */
    public ReceiveAndDumper(int sampaPort,
                           int streamId,
                           int streamFrameLimit,
                           SampaType sampaType) {
        this.sampaPort = sampaPort;
        this.streamId = streamId;
        this.streamFrameLimit = streamFrameLimit;
        this.sampaType = sampaType;

        frameBuffer = ByteBuffer.wrap(frameArray);
        frameBuffer.order(ByteOrder.LITTLE_ENDIAN);

        boolean verbose = false;

        if (sampaType.isDSP()) {
            sampaDecoder = new DspDecoder(verbose);
        }
        else {
            sampaDecoder = new DasDecoder(verbose, streamId);
        }
    }

    public void processOneFrame(SRingRawEvent rawEvent) {
        frameBuffer.clear();

        try {
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


    public void readAndDumpOneFrame() {
        try {
            dataInputStream.readFully(frameArray);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void run() {
        // Connecting to the sampa stream source
        try {
            ServerSocket serverSocket = new ServerSocket(sampaPort);
            System.out.println("Server is listening on port " + sampaPort);
            Socket socket = serverSocket.accept();
            System.out.println("SAMPA client connected");
            InputStream input = socket.getInputStream();
            dataInputStream = new DataInputStream(new BufferedInputStream(input, 65536));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int frameCount = 0;

        try {
            //SRingRawEvent rawEvent = new SRingRawEvent(SampaType.DAS);

            do {
                // Read another frame
                //rawEvent.reset();
                //processOneFrame(rawEvent);
                readAndDumpOneFrame();

                frameCount++;

                if (frameCount % 1000 == 0) {
                    System.out.println("Receiver " + streamId + ": read " + frameCount + " frames");
                }

                // Loop until we run into our given limit of frames
            } while (frameCount < streamFrameLimit);

            System.out.println("Receiver " + streamId + ": quitting as frame limit reached, " + streamFrameLimit);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


}
