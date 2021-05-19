package org.jlab.epsci.ersap.sampa;

import com.lmax.disruptor.*;

import java.nio.ByteBuffer;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;

public class SMPTwoStreamAggregatorDecoder {
    /**
     * SAMPA ports
     */
    private int sampaPort1;
    private int sampaPort2;

    /**
     * Max ring items
     */
    private final static int maxRingItems = 32768;

    /**
     * Ring buffers
     */
    private RingBuffer<SRingRawEvent> ringBuffer1;
    private RingBuffer<SRingRawEvent> ringBuffer2;
    private RingBuffer<SRingRawEvent> ringBuffer12;

    /**
     * Sequences
     */
    private Sequence sequence1;
    private Sequence sequence2;
    private Sequence sequence12;

    /**
     * Sequence barriers
     */
    private SequenceBarrier sequenceBarrier1;
    private SequenceBarrier sequenceBarrier2;
    private SequenceBarrier sequenceBarrier12;

    private SReceiver receiver1;
    private SReceiver receiver2;
    private SAggregator aggregator12;

    public SMPTwoStreamAggregatorDecoder(int sampaPort1, int sampaPort2) {
        this.sampaPort1 = sampaPort1;
        this.sampaPort2 = sampaPort2;

        ringBuffer1 = createSingleProducer(new SRingRawEventFactory(), maxRingItems,
                new YieldingWaitStrategy());
        sequence1 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier1 = ringBuffer1.newBarrier();
        ringBuffer1.addGatingSequences(sequence1);

        ringBuffer2 = createSingleProducer(new SRingRawEventFactory(), maxRingItems,
                new YieldingWaitStrategy());
        sequence2 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier2 = ringBuffer2.newBarrier();
        ringBuffer2.addGatingSequences(sequence2);

//        ringBuffer12 = createSingleProducer(new SRingRawEventFactory(), maxRingItems,
//                new YieldingWaitStrategy());
//        sequence12 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
//        sequenceBarrier12 = ringBuffer12.newBarrier();
//        ringBuffer12.addGatingSequences(sequence12);

    }

    public void go() {
        receiver1 = new SReceiver(sampaPort1, 1, ringBuffer1, 10);
        receiver2 = new SReceiver(sampaPort2, 2, ringBuffer2, 10);

        aggregator12 = new SAggregator(ringBuffer1, ringBuffer2, sequence1,
                sequence2, sequenceBarrier1, sequenceBarrier2, ringBuffer12);

        receiver1.start();
        receiver2.start();

//        aggregator12.start();
    }

    public void close() {
        receiver1.exit();
        receiver2.exit();
        aggregator12.exit();
    }

    public static void main(String[] args) {
        int port1 = Integer.parseInt(args[0]);
        int port2 = Integer.parseInt(args[1]);

        new SMPTwoStreamAggregatorDecoder(port1, port2).go();
    }
}
