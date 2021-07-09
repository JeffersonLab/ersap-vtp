package org.jlab.epsci.stream.sampaBB;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class StreamProcessor {

    private DataInputStream dataInputStream;
    private int streamId;
    // server socket
    private ServerSocket serverSocket;
    private final ByteBuffer headerBuffer;
    private final byte[] header = new byte[16];
    private final int[] data = new int[4];

    private final DspDecoder sampaDecoder;

    public StreamProcessor(int sampaPort, int streamId) {
        this.streamId = streamId;
        headerBuffer = ByteBuffer.wrap(header);
        headerBuffer.order(ByteOrder.LITTLE_ENDIAN);

        sampaDecoder = new DspDecoder();

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
        //Arrays.fill(data, 0);
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

        sampaDecoder.frameCount++;
        sampaDecoder.blockFrameCount++;

        // All data storage is now in the raw event object
        SRingRawEvent rawEvent = new SRingRawEvent();

        for (int eLink = 0; eLink < 28; eLink++) {
            try {
                sampaDecoder.decodeSerial(eLink, data, rawEvent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sampaDecoder.printBlockData(streamId, rawEvent);
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
        System.out.println("   num stream = " +streamId);
        System.out.println( "   block count = " +s.sampaDecoder.block_count) ;

    }
}