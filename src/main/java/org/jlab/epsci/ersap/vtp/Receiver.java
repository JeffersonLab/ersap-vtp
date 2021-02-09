package org.jlab.epsci.ersap.vtp;

import com.lmax.disruptor.RingBuffer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import static org.jlab.epsci.ersap.vtp.EUtil.printFrame;

/**
 * Receives stream frames from a VTP and writes them to a RingBuffer
 * <p>
 * ___        __
 * |   |      /  \
 * |   | ---> \  /
 * ---        --
 */
public class Receiver extends Thread {


    /**
     * VTP data stream
     */
    private DataInputStream dataInputStream;

    /**
     * Stream ID
     */
    private int streamId;

    /**
     * Output ring
     */
    private RingBuffer<RingEvent> ringBuffer;

    /**
     * Current spot in the ring from which an item was claimed.
     */
    private final AtomicLong getSequence = new AtomicLong();

    /**
     * For statistics
     */
    private int statLoop;
    private int statPeriod;
    private double totalData;
    private int rate;
    private AtomicLong missed_record;
    private long prev_rec_number;
    private ByteBuffer headerBuffer;
    private byte[] header = new byte[52];

    public Receiver(int vtpPort, int streamId, RingBuffer<RingEvent> ringBuffer, int statPeriod) {
        this.ringBuffer = ringBuffer;
        this.streamId = streamId;
        this.statPeriod = statPeriod;

        missed_record = new AtomicLong(0);

        headerBuffer = ByteBuffer.wrap(header);
        ;
        headerBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // Timer for measuring and printing statistics.
        Timer timer = new Timer();
        timer.schedule(new PrintRates(), 0, 1000);

        // Connecting to the VTP stream source
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(vtpPort);
            System.out.println("Server is listening on port " + vtpPort);
            Socket socket = serverSocket.accept();
            System.out.println("VTP client connected");
            InputStream input = socket.getInputStream();
//            dataInputStream = new DataInputStream(new BufferedInputStream(input));
            dataInputStream = new DataInputStream(new BufferedInputStream(input, 65536)); //CT suggestion
            dataInputStream.readInt();
            dataInputStream.readInt();
        } catch (
                IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Get the next available item in ring buffer for writing data.
     *
     * @return next available item in ring buffer.
     * @throws InterruptedException if thread interrupted.
     */
    private RingEvent get() throws InterruptedException {

        getSequence.set(ringBuffer.next());
        RingEvent buf = ringBuffer.get(getSequence.get());
        return buf;
    }

    private void publish() {
        ringBuffer.publish(getSequence.get());
    }


    private void decodeVtpHeader(RingEvent evt) {
        try {
            int source_id = Integer.reverseBytes(dataInputStream.readInt());
            int total_length = Integer.reverseBytes(dataInputStream.readInt());
            int payload_length = Integer.reverseBytes(dataInputStream.readInt());
            int compressed_length = Integer.reverseBytes(dataInputStream.readInt());
            int magic = Integer.reverseBytes(dataInputStream.readInt());

            int format_version = Integer.reverseBytes(dataInputStream.readInt());
            int flags = Integer.reverseBytes(dataInputStream.readInt());
            long record_number = EUtil.llSwap(Long.reverseBytes(dataInputStream.readLong()));
            long ts_sec = EUtil.llSwap(Long.reverseBytes(dataInputStream.readLong()));
            long ts_nsec = EUtil.llSwap(Long.reverseBytes(dataInputStream.readLong()));

//            BigInteger rcn = EUtil.toUnsignedBigInteger(record_number);
//                BigInteger tsc = EUtil.toUnsignedBigInteger(ts_sec);
//                BigInteger tsn = EUtil.toUnsignedBigInteger(ts_nsec);
//            printFrame(streamId, source_id, total_length, payload_length,
//                    compressed_length, magic, format_version, flags,
//                    record_number, ts_sec, ts_nsec);

            byte[] dataBuffer = new byte[payload_length];
            dataInputStream.readFully(dataBuffer);
            evt.setPayload(dataBuffer);
//            evt.setRecordNumber(rcn);
            evt.setRecordNumber(record_number);
            evt.setStreamId(streamId);

            // Collect statistics
            long tmp = missed_record.get() + (record_number - (prev_rec_number + 1));
            missed_record.set(tmp);

            prev_rec_number = record_number;
            totalData = totalData + (double) total_length / 1000.0;
            rate++;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void decodeVtpHeaderCT(RingEvent evt) {
        try {
            headerBuffer.clear();
            dataInputStream.readFully(header);

            int source_id = headerBuffer.getInt();
            int total_length = headerBuffer.getInt();
            int payload_length = headerBuffer.getInt();
            int compressed_length = headerBuffer.getInt();
            int magic = headerBuffer.getInt();

            int format_version = headerBuffer.getInt();
            int flags = headerBuffer.getInt();
            long record_number = EUtil.llSwap(headerBuffer.getLong());
            long ts_sec = EUtil.llSwap(headerBuffer.getLong());
            long ts_nsec = EUtil.llSwap(headerBuffer.getLong());

//            BigInteger rcn = EUtil.toUnsignedBigInteger(record_number);
//                BigInteger tsc = EUtil.toUnsignedBigInteger(ts_sec);
//                BigInteger tsn = EUtil.toUnsignedBigInteger(ts_nsec);
//            printFrame(streamId, source_id, total_length, payload_length,
//                    compressed_length, magic, format_version, flags,
//                    record_number, ts_sec, ts_nsec);

            if (evt.getPayload().length < payload_length) {
                byte[] payloadData = new byte[payload_length];
                evt.setPayload(payloadData);
            }
            dataInputStream.readFully(evt.getPayload(), 0, payload_length);

//            evt.setRecordNumber(rcn);
            evt.setPayloadDataLength(payload_length);
            evt.setRecordNumber(record_number);
            evt.setStreamId(streamId);

            // Collect statistics
            long tmp = missed_record.get() + (record_number - (prev_rec_number + 1));
            missed_record.set(tmp);

            prev_rec_number = record_number;
            totalData = totalData + (double) total_length / 1000.0;
            rate++;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            while (true) {
                // Get an empty item from ring
                RingEvent buf = get();

//                decodeVtpHeader(buf);
                decodeVtpHeaderCT(buf); //CT suggestion

                // Make the buffer available for consumers
                publish();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class PrintRates extends TimerTask {
        private long total_missed = 0;
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        @Override
        public void run() {
            if (statLoop <= 0) {
                System.out.println("\n" + dateFormat.format(new Date())
                        + "stream:" + streamId
                        + " event rate =" + rate / statPeriod
                        + " Hz.  data rate =" + totalData / statPeriod + " kB/s."
                        + " missed rate = " + missed_record.get() / statPeriod + " Hz."
                        + " total missed = " + total_missed
                );
                statLoop = statPeriod;
                rate = 0;
                totalData = 0;
                total_missed += missed_record.get();
                missed_record.set(0);
            }
            statLoop--;
        }
    }

}
