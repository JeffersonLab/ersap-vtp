package org.jlab.epsci.ersap.sampa;

import com.lmax.disruptor.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
    private int streamId;

    /**
     * Output ring
     */
    private RingBuffer<SRingRawEvent> ringBuffer;

    /**
     * Current spot in the ring from which an item was claimed.
     */
    private long sequenceNumber;

    // server socket
    private ServerSocket serverSocket;

    // control for the thread termination
    private AtomicBoolean running = new AtomicBoolean(true);

    /**
     * For statistics
     */
    private int sampaPort;
    private int statPeriod;
    private double totalData;
    private int rate;
    private long missed_record;
    private long prev_rec_number;
    private ByteBuffer headerBuffer;
    private byte[] header = new byte[8];

    public SReceiver(int sampaPort, int streamId, RingBuffer<SRingRawEvent> ringBuffer, int statPeriod) {
        this.sampaPort = sampaPort;
        this.ringBuffer = ringBuffer;
        this.streamId = streamId;
        this.statPeriod = statPeriod;

        headerBuffer = ByteBuffer.wrap(header);

        headerBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // Timer for measuring and printing statistics.
        Timer timer = new Timer();
        timer.schedule(new PrintRates(false), 0, statPeriod * 1000);
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

    private void decodeSAMPAHeader(SRingRawEvent evt) {
        try {
            headerBuffer.clear();
            dataInputStream.readFully(header);

            // Header word 1
            int h1  = headerBuffer.getInt();
            int chipAddress =  h1 & 0x0000000f;
            int channelAddress =  (h1 & 0x000001f0) >>> 4;
            int windowTime = (h1 >>> 9) & 0x000fffff;

            // Header word 2
            int h2 = headerBuffer.getInt();
            int numberDataWords = (h2 >>> 3) & 0x000003ff;
            int payload_length = numberDataWords * 4;

            if (evt.getPayload().length < payload_length) {
                byte[] payloadData = new byte[payload_length];
                evt.setPayload(payloadData);
            }
            dataInputStream.readFully(evt.getPayload(), 0, payload_length);

           // Debug printout
                System.out.println("DDD: streamId = "+ streamId +
                        " chip = " + chipAddress +
                        " channel = " + channelAddress +
                        " startTime = " + windowTime +
                        " dataWords = " + numberDataWords);
            try {
                this.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            evt.setPayloadDataLength(payload_length);
            evt.setChipAddress(chipAddress);
            evt.setChannelAddress(channelAddress);
            evt.setWindowTime(windowTime);

            int record_number = 0;
            // Collect statistics
            missed_record = missed_record + (record_number - (prev_rec_number + 1));

            prev_rec_number = record_number;
            totalData = totalData + (double) payload_length / 1000.0;
            rate++;

        } catch (IOException e) {
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
        } catch (
                IOException e) {
            e.printStackTrace();
        }

        while (running.get()) {
            try {
                // Get an empty item from ring
                SRingRawEvent buf = get();

                decodeSAMPAHeader(buf);

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
        BufferedWriter bw;
        boolean f_out;

        public PrintRates(boolean file_out) {
            f_out = file_out;
            if (f_out) {
                try {
                    bw = new BufferedWriter(new FileWriter("stream_" + streamId + ".csv"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            long m_rate = missed_record / statPeriod;
            if (f_out) {
                try {
                    bw.write(m_rate + "\n");
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(" stream:" + streamId
                    + " event rate =" + rate / statPeriod
                    + " Hz.  data rate =" + totalData / statPeriod + " kB/s."
                    + " missed rate = " + m_rate + " Hz."
                    + " record number = " + prev_rec_number
            );
            rate = 0;
            totalData = 0;
            missed_record = 0;
        }
    }

}
