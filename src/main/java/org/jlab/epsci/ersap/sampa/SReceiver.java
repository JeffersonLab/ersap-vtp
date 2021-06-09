package org.jlab.epsci.ersap.sampa;

import com.lmax.disruptor.RingBuffer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class SReceiver extends Thread {

    private DataInputStream dataInputStream;
    private final int streamId;
    private final int streamFrameLimit;
    // server socket
    private ServerSocket serverSocket;
    private final ByteBuffer headerBuffer;
    private final byte[] header = new byte[16];
    private final int[] data = new int[4];

    private final SDecoder sampaDecoder;

    // Output disruptor ring buffer
    private final RingBuffer<SRingRawEvent> ringBuffer;
    // Current spot in the ring from which an item was claimed.
    private long sequenceNumber;

    public SReceiver(int sampaPort,
                     int streamId,
                     RingBuffer<SRingRawEvent> ringBuffer,
                     int streamFrameLimit) {
        this.ringBuffer = ringBuffer;
        this.streamId = streamId;
        this.streamFrameLimit = streamFrameLimit;

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

    public void process(SRingRawEvent rawEvent) {
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

        sampaDecoder.frameCount = sampaDecoder.frameCount + 1;
        sampaDecoder.block_frameCount = sampaDecoder.block_frameCount + 1;

        for (int eLink = 0; eLink < 28; eLink++) {
            sampaDecoder.decodeSerial(eLink, data);
        }
        // update ring event with the decoded data
        sampaDecoder.getBlockData(rawEvent);

//        sampaDecoder.printBlockData(streamId);
    }

    public void run() {
        for(int i = 0; i < streamFrameLimit; i++) {
            try {
                // Get an empty item from ring
                SRingRawEvent sRawEvent = get();
                sRawEvent.reset();

                process(sRawEvent);

                // Make the buffer available for consumers
                publish();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        sampaDecoder.printLinkStats();
        exit();
    }

    public void exit() {
        try {
            dataInputStream.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.interrupt();
    }
}