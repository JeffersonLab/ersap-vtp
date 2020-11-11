package org.jlab.epsci.ersap.vtp;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ReceiverTest {
    /**
     * VTP data stream
     */
    private DataInputStream dataInputStream;

    /**
     * Stream ID
     */
    private final int streamId;

    /**
     * For statistics
     */
    private int statLoop;
    private final int statPeriod;
    private double totalData;
    private int rate;
    private long missed_record;
    private long prev_rec_number;


    public ReceiverTest(int vtpPort, int streamId,  int statPeriod) {
        this.streamId = streamId;
        this.statPeriod = statPeriod;

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
            dataInputStream = new DataInputStream(new BufferedInputStream(input));
            dataInputStream.readInt();
            dataInputStream.readInt();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
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

            BigInteger rcn = EUtil.toUnsignedBigInteger(record_number);
            System.out.println(streamId + " ===================================================");
            System.out.println(String.format("source ID = %d", source_id));
            System.out.println(String.format("total_length = %d", total_length));
            System.out.println(String.format("payload_length = %d", payload_length));
            System.out.println(String.format("compressed_length = %d", compressed_length));
            System.out.println(String.format("magic = %x", magic));
            System.out.println(String.format("format_version = %d", format_version));
            System.out.println(String.format("flags = %d", flags));
            System.out.println(String.format("record_number = %d", record_number));
            System.out.println(String.format("ts_sec = %d", ts_sec));
            System.out.println(String.format("ts_nsec = %d", ts_nsec));

            byte[] dataBuffer = new byte[payload_length];
            dataInputStream.readFully(dataBuffer);

            // Collect statistics
            missed_record = missed_record + (record_number - (prev_rec_number + 1));
            prev_rec_number = record_number;
            totalData = totalData + (double) total_length / 1000.0;
            rate++;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        ReceiverTest rt = new ReceiverTest(port, 1, 10);
        while (true) {
            rt.decodeVtpHeader();
        }

    }
        private class PrintRates extends TimerTask {

            @Override
            public void run() {
                if (statLoop <= 0) {
                    System.out.println("stream:" + streamId
                            + " event rate =" + rate / statPeriod
                            + " Hz.  data rate =" + totalData / statPeriod + " kB/s."
                            + " missed rate = " + missed_record / statPeriod + " Hz."
                    );
                    statLoop = statPeriod;
                    rate = 0;
                    totalData = 0;
                    missed_record = 0;
                }
                statLoop--;
            }

        }
    }