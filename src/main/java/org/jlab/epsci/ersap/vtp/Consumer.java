package org.jlab.epsci.ersap.vtp;

import com.lmax.disruptor.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.jlab.epsci.ersap.vtp.EUtil.*;

public class Consumer extends Thread {
    private RingBuffer<RingEvent> ringBuffer;
    private Sequence sequence;
    private SequenceBarrier barrier;

    private long nextSequence;
    private long availableSequence;

    /**
     * Consumer constructor
     *
     * @param ringBuffer
     * @param sequence
     * @param barrier
     * @param runNumber
     */
    public Consumer(RingBuffer<RingEvent> ringBuffer,
                    Sequence sequence,
                    SequenceBarrier barrier,
                    int runNumber) {

        this.ringBuffer = ringBuffer;
        this.sequence = sequence;
        this.barrier = barrier;

        ringBuffer.addGatingSequences(sequence);
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
    public RingEvent get() throws InterruptedException {

        RingEvent item = null;

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

    public void run() {
        HitFinder hitFinder = new HitFinder();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {

            while (true) {
                // Get an empty item from ring
                RingEvent buf = get();

//                BigInteger frameTime =
//                        buf.getRecordNumber().multiply(EUtil.toUnsignedBigInteger(65536L));
//                byte[] payload = buf.getPayload();
//                if (payload.length > 0) {
//                    Runnable r = () -> decodePayloadMap(frameTime, payload);
//                    pool.execute(r);
//                    List<AdcHit> evt = decodePayload(frameTime, payload);
//                    Map<Integer, List<ChargeTime>> hits = hitFinder
//                            .reset()
//                            .stream(evt)
//                            .frameStartTime(frameTime)
//                            .frameLength(64000)
//                            .sliceSize(32)
//                            .windowSize(4)
//                            .slide();
//                    printHits(hits);
//                }

                put();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
