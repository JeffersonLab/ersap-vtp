package org.jlab.epsci.ersap.sampa;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class StreamProcessor {

    private DataInputStream dataInputStream;
    private int streamId;
    // server socket
    private ServerSocket serverSocket;
    private final ByteBuffer headerBuffer;
    private final byte[] header = new byte[16];
    private final int[] data = new int[4];

    private final SDecoder sampaDecoder;

    public StreamProcessor(int sampaPort, int streamId) {
        this.streamId = streamId;
        headerBuffer = ByteBuffer.wrap(header);
        headerBuffer.order(ByteOrder.LITTLE_ENDIAN);

        sampaDecoder = new SDecoder();

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
    }

    public void process() {
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

//        System.out.println(" w3 =" + Integer.toHexString(data[3])
//                + " w2 =" + Integer.toHexString(data[2])
//                + " w1 =" + Integer.toHexString(data[1])
//                + " w0 =" + Integer.toHexString(data[0])
//        );

        for (int eLink = 0; eLink < 28; eLink++) {
            System.out.println("=========================" + eLink + "========================");
            sampaDecoder.decodeSerial(eLink, data);
        }
        sampaDecoder.printBlockData(streamId);
    }

    public void test() {
        int h1 = 0xb6e08000;
        int chipAddress = h1 & 0x0000000f;
        int channelAddress = (h1 & 0x000001f0) >>> 4;
        int windowTime = (h1 >>> 9) & 0x000fffff;

        int h2 = 0x406200b4;
        int numberDataWords = (h2 >>> 3) & 0x000003ff;

        System.out.println("DDD:  chip = " + chipAddress +
                " channel = " + channelAddress +
                " startTime = " + windowTime +
                " dataWords = " + numberDataWords);

    }

    public static void main(String[] args) {
        int port1 = Integer.parseInt(args[0]);
        int streamId = Integer.parseInt(args[1]);
        int streamFrameLimit = Integer.parseInt(args[2]);
        StreamProcessor s = new StreamProcessor(port1, streamId);
        for(int i= 0; i < streamFrameLimit; i++) {
            s.process();
        }
        s.sampaDecoder.printLinkStats();
    }
}