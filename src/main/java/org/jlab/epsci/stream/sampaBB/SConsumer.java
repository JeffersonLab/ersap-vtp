/*
 * Copyright (c) 2021, Jefferson Science Associates, all rights reserved.
 * See LICENSE.txt file.
 *
 * Thomas Jefferson National Accelerator Facility
 * Experimental Physics Software and Computing Infrastructure Group
 *
 * 12000, Jefferson Ave, Newport News, VA 23606
 * Phone : (757)-269-7100
 */

package org.jlab.epsci.stream.sampaBB;

import com.lmax.disruptor.*;

import java.util.concurrent.atomic.AtomicBoolean;

public class SConsumer extends Thread {
    private RingBuffer<SRingRawEvent> ringBuffer;
    private Sequence sequence;
    private SequenceBarrier barrier;
    private long nextSequence;
    private long availableSequence;

    // control for the thread termination
    private AtomicBoolean running = new AtomicBoolean(true);

    public SConsumer(RingBuffer<SRingRawEvent> ringBuffer,
                     Sequence sequence,
                     SequenceBarrier barrier) {
        this.ringBuffer = ringBuffer;
        this.sequence = sequence;
        this.barrier = barrier;

        nextSequence = sequence.get() + 1L;
        availableSequence = -1L;
    }

    /**
     * Get the next available item from output ring buffer.
     * Do NOT call this multiple times in a row!
     * Be sure to call "put" before calling this again.
     *
     * @return next available item in ring buffer.
     * @throws InterruptedException e
     */
    public SRingRawEvent get() throws InterruptedException {

        SRingRawEvent item = null;

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

    public void run() {

        while (running.get()) {
            try {

                // Get an item from ring and parse the payload
                SRingRawEvent buf = get();
                put();

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
