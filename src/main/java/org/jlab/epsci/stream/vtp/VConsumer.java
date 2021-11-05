package org.jlab.epsci.stream.vtp;

import com.lmax.disruptor.*;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jlab.epsci.stream.util.commons.PayloadDecoderFactory;
import org.jlab.epsci.stream.util.commons.PayloadDecoderPool;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.jlab.epsci.stream.util.EUtil.*;

public class VConsumer extends Thread {
    private RingBuffer<VRingRawEvent> ringBuffer;
    private Sequence sequence;
    private SequenceBarrier barrier;
    private long nextSequence;
    private long availableSequence;


    // control for the thread termination
    private AtomicBoolean running = new AtomicBoolean(true);

    private ExecutorService tPool;
    private PayloadDecoderPool pool;

    public VConsumer(RingBuffer<VRingRawEvent> ringBuffer,
                     Sequence sequence,
                     SequenceBarrier barrier,
                     int runNumber) {

        this.ringBuffer = ringBuffer;
        this.sequence = sequence;
        this.barrier = barrier;

//        ringBuffer.addGatingSequences(sequence);
        nextSequence = sequence.get() + 1L;
        availableSequence = -1L;

        tPool = Executors.newFixedThreadPool(128);
        pool = createPdPool(128);

    }

    /**
     * Get the next available item from output ring buffer.
     * Do NOT call this multiple times in a row!
     * Be sure to call "put" before calling this again.
     *
     * @return next available item in ring buffer.
     * @throws InterruptedException e
     */
    public VRingRawEvent get() throws InterruptedException {

        VRingRawEvent item = null;

        try {
            if (availableSequence < nextSequence) {
                availableSequence = barrier.waitFor(nextSequence);
            }

            item = ringBuffer.get(nextSequence);
        } catch (final TimeoutException | AlertException ex) {
            // never happen since we don't use timeout wait strategy
            ex.printStackTrace();
        }

        return item;
    }

     public void put() throws InterruptedException {

        // Tell input (crate) ring that we're done with the item we're consuming
        sequence.set(nextSequence);

        // Go to next item to consume on input ring
        nextSequence++;
    }

    private PayloadDecoderPool createPdPool(int size) {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxIdle(1);
        config.setMaxTotal(size);


        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        return new PayloadDecoderPool(new PayloadDecoderFactory(), config);
    }

    public void run() {

        while (running.get()) {

//                BigInteger frameTime =
//                        buf.getRecordNumber().multiply(EUtil.toUnsignedBigInteger(65536L));
            try {

                // Get an item from ring and parse the payload
                VRingRawEvent buf = get();
//                put();

                if (buf.getPayload().length > 0) {
                    long frameTime = buf.getRecordNumber() * 65536L;
                    ByteBuffer b = cloneByteBuffer(buf.getPayloadBuffer());
                    put();
//                    Runnable r = () -> decodePayloadMap3(frameTime, b, 0, buf.getPartLength1() / 4);

                    // using object pool
//                    Runnable r = () -> {
//                        try {
//                            VPayloadDecoder pd = pool.borrowObject();
//                            pd.decode(frameTime, b, 0, buf.getPartLength1() / 4);
//                            pool.returnObject(pd);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    };
//                    tPool.execute(r);

                } else {
                    put();
                }


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public ByteBuffer getEvent() throws Exception {
        ByteBuffer out;
        VPayloadDecoder pd = pool.borrowObject();
        out = pd.getEvt();
        pool.returnObject(pd);
        return out;
    }

    public void exit() {
        running.set(false);
        this.interrupt();
    }

}
