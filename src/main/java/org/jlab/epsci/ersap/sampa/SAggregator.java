package org.jlab.epsci.ersap.sampa;

import com.lmax.disruptor.*;
import org.jlab.epsci.ersap.util.EUtil;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * __
 * /  \
 * \  /     ___       __
 * --  --> |   |     /  \
 * __      |   |-->  \  /
 * /  \ --> ---       --
 * \  /
 * --
 */
public class SAggregator extends Thread {
    /**
     * Maps for aggregation
     */
    private final HashMap<Integer, byte[]> m1 = new HashMap<>();
    private final HashMap<Integer, byte[]> m2 = new HashMap<>();

    /**
     * Current spot in output ring from which an item was claimed.
     */
    private long outSequence;

    /**
     * 1 RingBuffer per stream.
     */
    private final RingBuffer<SRingRawEvent> ringBuffer1;
    private final RingBuffer<SRingRawEvent> ringBuffer2;

    /**
     * 1 sequence per stream
     */
    private final Sequence sequence1;
    private final Sequence sequence2;

    /**
     * 1 barrier per stream
     */
    private final SequenceBarrier barrier1;
    private final SequenceBarrier barrier2;

    /**
     * Track which sequence the aggregating consumer wants next from each of the crate rings.
     */
    private long nextSequence1;
    private long nextSequence2;

    /**
     * Track which sequence is currently available from each of the crate rings.
     */
    private long availableSequence1;
    private long availableSequence2;

    /**
     * 1 output RingBuffer.
     */
    private final RingBuffer<SRingRawEvent> outputRingBuffer;

    // control for the thread termination
    private final AtomicBoolean running = new AtomicBoolean(true);

    public SAggregator(RingBuffer<SRingRawEvent> ringBuffer1, RingBuffer<SRingRawEvent> ringBuffer2,
                       Sequence sequence1, Sequence sequence2,
                       SequenceBarrier barrier1, SequenceBarrier barrier2,
                       RingBuffer<SRingRawEvent> outputRingBuffer) {

        this.ringBuffer1 = ringBuffer1;
        this.ringBuffer2 = ringBuffer2;
        this.sequence1 = sequence1;
        this.sequence2 = sequence2;
        this.barrier1 = barrier1;
        this.barrier2 = barrier2;
        this.outputRingBuffer = outputRingBuffer;

        nextSequence1 = sequence1.get() + 1L;
        nextSequence2 = sequence2.get() + 1L;

        availableSequence1 = -1L;
        availableSequence2 = -1L;
    }

    public void get() throws InterruptedException {

        try {
            if (availableSequence1 < nextSequence1) {
                availableSequence1 = barrier1.waitFor(nextSequence1);
            }
            SRingRawEvent inputItem1 = ringBuffer1.get(nextSequence1);

            if (availableSequence2 < nextSequence2) {
                availableSequence2 = barrier2.waitFor(nextSequence2);
            }
            SRingRawEvent inputItem2 = ringBuffer2.get(nextSequence2);
/*
// @todo aggregation. should be a separate method.

            int b1 = inputItem1.getWindowTime();
            int b2 = inputItem2.getWindowTime();

            int l1 = inputItem1.getPayload().length;
            int l2 = inputItem2.getPayload().length;

            m1.put(b1, inputItem1.getPayload());
            m2.put(b2, inputItem2.getPayload());

            if (m1.containsKey(b1) && m2.containsKey(b1)) {
                outSequence = outputRingBuffer.next();
                SRingRawEvent outputItem = outputRingBuffer.get(outSequence);

                EUtil.addByteArrays(m1.get(b1), l1, m2.get(b1), l2, outputItem.getPayload());

                outputItem.setWindowTime(b1);
                outputRingBuffer.publish(outSequence);
                m1.remove(b1);
                m2.remove(b1);
            }

            if (m1.containsKey(b2) && m2.containsKey(b2)) {

                outSequence = outputRingBuffer.next();
                SRingRawEvent outputItem = outputRingBuffer.get(outSequence);

                EUtil.addByteArrays(m1.get(b2), l1, m2.get(b2), l2, outputItem.getPayload());
                outputItem.setWindowTime(b2);
                outputRingBuffer.publish(outSequence);
                m1.remove(b2);
                m2.remove(b2);
            }

 */
            outSequence = outputRingBuffer.next();
            SRingRawEvent outputItem = outputRingBuffer.get(outSequence);

            outputRingBuffer.publish(outSequence);
        } catch (final TimeoutException | AlertException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * This "consumer" is also a producer for the output ring.
     * So get items from the output ring and fill them with items claimed from the input rings.
     */
    private void put() throws InterruptedException {
        sequence1.set(nextSequence1);
        nextSequence1++;

        sequence2.set(nextSequence2);
        nextSequence2++;
    }

    public void run() {
        try {
            while (running.get()) {
                get();
                put();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void exit() {
        running.set(false);
        this.interrupt();
    }

}
