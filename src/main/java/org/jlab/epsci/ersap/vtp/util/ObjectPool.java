package org.jlab.epsci.ersap.vtp.util;

import com.lmax.disruptor.*;
import org.jlab.epsci.ersap.vtp.PayloadDecoder;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;

public class ObjectPool {
    private final RingBuffer<PayloadDecoder> ringBuffer;
    private final Sequence sequence;
    private final SequenceBarrier sequenceBarrier;
    private long nextSequence;
    private long availableSequence;

    /**
     * Creates an object pool that reuses a fixed number of objects operating off a LMAX disruptor ring.
     * At any point, at most nObjects objects will be active processing tasks. If additional tasks are
     * submitted when all Objects are active, they will wait in the queue until an object is available.
     * Objects in the pool will exist until it is explicitly shutdown (no GC before that).
     *
     * @param factory instance of a processor class
     * @param depth      number of objects in the pool
     */
    public ObjectPool(EventFactory<PayloadDecoder> factory, int depth) {

        ringBuffer = createSingleProducer(factory, depth,
                new YieldingWaitStrategy());
        sequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier = ringBuffer.newBarrier();
        ringBuffer.addGatingSequences(sequence);

        nextSequence = sequence.get() + 1L;
        availableSequence = -1L;

    }

    /**
     * Get the next available item from the object pool.
     *
     * @return next available object in ring buffer.
     * @throws InterruptedException exception
     */
    public synchronized PayloadDecoder get() throws InterruptedException {

        PayloadDecoder item = null;

        try {
            if (availableSequence < nextSequence) {
                availableSequence = sequenceBarrier.waitFor(nextSequence);
            }

            item = ringBuffer.get(nextSequence);
        } catch (final TimeoutException | AlertException ex) {
            // never happen since we don't use timeout wait strategy
            ex.printStackTrace();
        }
        return item;
    }

    public synchronized void put() throws InterruptedException {
        sequence.set(nextSequence);
        // Go to next object to return on the ring
        nextSequence++;
    }

}
