package org.jlab.epsci.ersap.sampa;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.jlab.epsci.ersap.util.EUtil.decodeSampaSerial;

public class SampaDecoder {

    private DataInputStream dataInputStream;
    // server socket
    private ServerSocket serverSocket;
    private final int sampaPort;
    private final ByteBuffer headerBuffer;
    byte[] header = new byte[16];
    int[] data = new int[4];

    public SampaDecoder(int sampaPort) {
        this.sampaPort = sampaPort;
        headerBuffer = ByteBuffer.wrap(header);
        headerBuffer.order(ByteOrder.LITTLE_ENDIAN);

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

    public void decodeSampa() {
        // clear gbt_frame: 4 4-byte, 32-bit words
        for (int i = 0; i < 4; i++) {
            data[i] = 0;
        }
        headerBuffer.clear();

        try {
            dataInputStream.readFully(header);
        } catch (IOException e) {
            e.printStackTrace();
        }
        data[3] = headerBuffer.getInt();
        data[2] = headerBuffer.getInt();
        data[1] = headerBuffer.getInt();
        data[0] = headerBuffer.getInt();

        System.out.println(" w3 =" + String.format("0x%08X", data[3])
                + " w2 =" + String.format("0x%08X", data[2])
                + " w1 =" + String.format("0x%08X", data[1])
                + " w0 =" + String.format("0x%08X", data[0])
        );

        for (int eLink = 0; eLink < 28; eLink++) {
            decodeSampaSerial(eLink, data);
        }

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
        SampaDecoder s = new SampaDecoder(port1);
        while(true){
           s.decodeSampa();
        }

    }
}