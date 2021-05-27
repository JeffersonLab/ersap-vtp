package org.jlab.epsci.ersap.sampa;

import com.lmax.disruptor.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Receives stream packets from a single of the SAMPA based
 * FEC (front-end readout card) and writes them to a RingBuffer
 * <p>
 * ___        __
 * |   |      /  \
 * |   | ---> \  /
 * ---        --
 */
public class SReceiver extends Thread {

    /**
     * SAMPA data stream
     */
    private DataInputStream dataInputStream;

    /**
     * Stream ID
     */
    private final int streamId;

    /**
     * Output ring
     */
    private final RingBuffer<SRingRawEvent> ringBuffer;

    /**
     * Current spot in the ring from which an item was claimed.
     */
    private long sequenceNumber;

    // server socket
    private ServerSocket serverSocket;

    // control for the thread termination
    private final AtomicBoolean running = new AtomicBoolean(true);

    /**
     * For statistics
     */
    private final int sampaPort;
    private final int statPeriod;
    private double totalData;
    private int packetNumber;
    private final ByteBuffer headerBuffer;
    byte[] header = new byte[16];
    int[] data = new int[4];

    private final SDecoder decoder;


    public SReceiver(int sampaPort, int streamId, RingBuffer<SRingRawEvent> ringBuffer, int statPeriod) {
        this.sampaPort = sampaPort;
        this.ringBuffer = ringBuffer;
        this.streamId = streamId;
        this.statPeriod = statPeriod;

        headerBuffer = ByteBuffer.wrap(header);
        headerBuffer.order(ByteOrder.LITTLE_ENDIAN);

        decoder = new SDecoder();

        // Timer for measuring and printing statistics.
        Timer timer = new Timer();
        timer.schedule(new PrintRates(), 0, statPeriod * 1000L);
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

    private void decodeSampa(SRingRawEvent evt) {
        Arrays.fill(data, 0);
        headerBuffer.clear();

       try {
            // clear gbt_frame: 4 4-byte, 32-bit words
            dataInputStream.readFully(header);
        } catch (IOException e) {
            e.printStackTrace();
        }
        data[3] = headerBuffer.getInt();
        data[2] = headerBuffer.getInt();
        data[1] = headerBuffer.getInt();
        data[0] = headerBuffer.getInt();

        System.out.println(" stream:" + streamId
                + " w3 =" + String.format("0x%08X", data[3])
                + " w2 =" + String.format("0x%08X", data[2])
                + " w1 =" + String.format("0x%08X", data[1])
                + " w0 =" + String.format("0x%08X", data[0])
        );

        for (int eLink = 0; eLink < 28; eLink++) {
            decoder.decodeSerial(eLink, data);
        }
    }

/*
    private void decodeSAMPAHeader(SRingRawEvent evt) {
        try {
            headerBuffer.clear();
            dataInputStream.readFully(header);
            int header_id;
            int h1;

            ////////////////// debug //////////////
            for (int i = 0; i < 100; i++) {
                h1 = headerBuffer.getInt();
            header_id = h1 >>> 29;
            System.out.println("DDD: streamId = " + streamId + " " + Integer.toHexString(h1) +
                    " header_id = " + header_id);
        }

            System.exit(1);
            ////////////////////////////////////////////////////

            do {
                h1 = headerBuffer.getInt();
                header_id = h1 >>> 28;
            } while (header_id == 5);

            // Header word 1. ID = 5
            int chipAddress = h1 & 0x0000000f;
            int channelAddress = (h1 >>> 4) & 0x0000001f;
            int windowTime = (h1 >>> 9) & 0x000fffff;

            // Header word 2. ID = 2
            int h2 = headerBuffer.getInt();
            header_id = h2 >>> 28;
            if (header_id == 2) {
                int pkt = h2 & 0x00000007;

                // At this point we are only interested in packets of type 4
                if (pkt == 4) {
                    System.out.println("DDD packet = " + pkt);
                    return;
                }
                int numberDataWords = (h2 >>> 3) & 0x000003ff;

                int payload_length = numberDataWords * 4;
                if (evt.getPayload().length < payload_length) {
                    byte[] payloadData = new byte[payload_length];
                    evt.setPayload(payloadData);
                }
                dataInputStream.readFully(evt.getPayload(), 0, payload_length);

                totalData = totalData + (double) payload_length / 1000.0;
                packetNumber++;

                // Debug printout
                System.out.println("DDD: streamId = " + streamId +
                        " chip = " + chipAddress +
                        " channel = " + channelAddress +
                        " startTime = " + windowTime +
                        " dataWords = " + numberDataWords);

                evt.setChipAddress(chipAddress);
                evt.setChannelAddress(channelAddress);
                evt.setWindowTime(windowTime);
                evt.setPayloadDataLength(payload_length);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/

    public void run() {
        // Connecting to the sampa stream source
        try {
            serverSocket = new ServerSocket(sampaPort);
            System.out.println("Server is listening on port " + sampaPort);
            Socket socket = serverSocket.accept();
            System.out.println("SAMPA client connected");
            InputStream input = socket.getInputStream();
            dataInputStream = new DataInputStream(new BufferedInputStream(input, 65536));
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        while (running.get()) {
            try {
                // Get an empty item from ring
                SRingRawEvent buf = get();

                decodeSampa(buf);

                // Make the buffer available for consumers
                publish();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void exit() {
        running.set(false);
        try {
            dataInputStream.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.interrupt();
    }

    private class PrintRates extends TimerTask {

        @Override
        public void run() {
//            System.out.println(" stream:" + streamId
//                    + " event rate =" + packetNumber / statPeriod
//                    + " Hz.  data rate =" + totalData / statPeriod + " kB/s."
//            );
            packetNumber = 0;
            totalData = 0;
        }
    }


}
