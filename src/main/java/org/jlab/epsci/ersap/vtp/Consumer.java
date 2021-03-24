package org.jlab.epsci.ersap.vtp;

import com.lmax.disruptor.*;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jlab.epsci.ersap.vtp.util.commons.PayloadDecoderFactory;
import org.jlab.epsci.ersap.vtp.util.commons.PayloadDecoderPool;
import org.jlab.epsci.ersap.vtp.util.disruptor.PDPool;
import org.jlab.epsci.ersap.vtp.util.disruptor.PDFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;
import static org.jlab.epsci.ersap.vtp.util.EUtil.*;

public class Consumer extends Thread {
    private RingBuffer<RingRawEvent> ringBuffer;
    private Sequence sequence;
    private SequenceBarrier barrier;
    private long nextSequence;
    private long availableSequence;


    // control for the thread termination
    private AtomicBoolean running = new AtomicBoolean(true);

    /**
     * Consumer constructor
     *
     * @param ringBuffer
     * @param sequence
     * @param barrier
     * @param runNumber
     */
    public Consumer(RingBuffer<RingRawEvent> ringBuffer,
                    Sequence sequence,
                    SequenceBarrier barrier,
                    int runNumber) {

        this.ringBuffer = ringBuffer;
        this.sequence = sequence;
        this.barrier = barrier;

//        ringBuffer.addGatingSequences(sequence);
        nextSequence = sequence.get() + 1L;
        availableSequence = -1L;

    }

    /**
     * Get the next available item from outupt ring buffer.
     * Do NOT call this multiple times in a row!
     * Be sure to call "put" before calling this again.
     *
     * @return next available item in ring buffer.
     * @throws InterruptedException
     */
    public RingRawEvent get() throws InterruptedException {

        RingRawEvent item = null;

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

    /**
     * This "consumer" is also a producer for the output ring.
     * So get items from the output ring and fill them with items claimed from the input rings.
     */
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
        ExecutorService tPool = Executors.newFixedThreadPool(64);
        PayloadDecoderPool pool = createPdPool(64);

        while (running.get()) {

//                BigInteger frameTime =
//                        buf.getRecordNumber().multiply(EUtil.toUnsignedBigInteger(65536L));
            try {

                // Get an empty item from ring and parse the payload
                RingRawEvent buf = get();
                if (buf.getPayload().length > 0) {
                    long frameTime = buf.getRecordNumber() * 65536L;
                    ByteBuffer b = cloneByteBuffer(buf.getPayloadBuffer());
                    put();
//                    Runnable r = () -> decodePayloadMap3(frameTime, b, 0, buf.getPartLength1() / 4);

                    Runnable r = () -> {
                        try {
                            PayloadDecoder pd = pool.borrowObject();
                            pd.decode(frameTime, b, 0, buf.getPartLength1() / 4);
                            pool.returnObject(pd);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    };

                    tPool.execute(r);
                } else {
                    put();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void exit() {
        running.set(false);
        this.interrupt();
    }

}
