package org.jlab.epsci.ersap.vtp;

import com.lmax.disruptor.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;

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
    private long getSequence; // This does NOT have to be atomic, Carl T

    /**
     * For statistics
     */
    private int statLoop;
    private int statPeriod;
    private double totalData;
    private int rate;
    private long missed_record;
    private long prev_rec_number;
    private ByteBuffer headerBuffer;
    private byte[] header = new byte[52];

    public Receiver(int vtpPort, int streamId, RingBuffer<RingEvent> ringBuffer, int statPeriod) {
        this.ringBuffer = ringBuffer;
        this.streamId = streamId;
        this.statPeriod = statPeriod;

        headerBuffer = ByteBuffer.wrap(header);
        ;
        headerBuffer.order(ByteOrder.LITTLE_ENDIAN);

        // Timer for measuring and printing statistics.
        Timer timer = new Timer();
        timer.schedule(new PrintRates(true), 0, statPeriod * 1000);

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

        getSequence = ringBuffer.next();
        RingEvent buf = ringBuffer.get(getSequence);
        return buf;
    }

    private void publish() {
        ringBuffer.publish(getSequence);
    }


    private void decodeVtpHeader() {
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
//            evt.setPayload(dataBuffer);
//            evt.setRecordNumber(rcn);
//            evt.setRecordNumber(record_number);
//            evt.setStreamId(streamId);

            // Collect statistics
            long tmp = missed_record + (record_number - (prev_rec_number + 1));
            missed_record = tmp;

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
//            long record_number = headerBuffer.getLong();
//            long ts_sec = headerBuffer.getLong();
//            long ts_nsec = headerBuffer.getLong();

//            printFrame(streamId, source_id, total_length, payload_length,
//                    compressed_length, magic, format_version, flags,
//                    record_number, ts_sec, ts_nsec);

            if (evt.getPayload().length < payload_length){
                byte[] payloadData = new byte[payload_length];
                evt.setPayload(payloadData);
            }
            dataInputStream.readFully(evt.getPayload(), 0, payload_length);

//            evt.setRecordNumber(rcn);
            evt.setPayloadDataLength(payload_length);
            evt.setRecordNumber(record_number);
            evt.setStreamId(streamId);

            // Collect statistics
            long tmp = missed_record + (record_number - (prev_rec_number + 1));
            missed_record = tmp;

            prev_rec_number = record_number;
            totalData = totalData + (double) total_length / 1000.0;
            rate++;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
            while (true) {
                try {
                // Get an empty item from ring
                RingEvent buf = get();

//                decodeVtpHeader();
                decodeVtpHeaderCT(buf); //CT suggestion

                // Make the buffer available for consumers
                publish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
    }

    private class PrintRates extends TimerTask {
        BufferedWriter bw;
        boolean f_out;

  public PrintRates(boolean file_out) {
      f_out = file_out;
      if(f_out) {
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
