package org.jlab.epsci.ersap.vtp;

import com.lmax.disruptor.*;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;

public class FourStreamAggregator {

    /**
     * VTP ports
     */
    private int vtpPort1;
    private int vtpPort2;
    private int vtpPort3;
    private int vtpPort4;

    /**
     * Max ring items
     */
    private final static int maxRingItems = 16;

    /**
     * Ring buffers
     */
    private RingBuffer<RingEvent> ringBuffer1;
    private RingBuffer<RingEvent> ringBuffer2;
    private RingBuffer<RingEvent> ringBuffer3;
    private RingBuffer<RingEvent> ringBuffer4;
    private RingBuffer<RingEvent> ringBuffer12;
    private RingBuffer<RingEvent> ringBuffer34;
    private RingBuffer<RingEvent> ringBuffer1234;

    /**
     * Sequences
     */
    private Sequence sequence1;
    private Sequence sequence2;
    private Sequence sequence3;
    private Sequence sequence4;
    private Sequence sequence12;
    private Sequence sequence34;
    private Sequence sequence1234;

    /**
     * Sequence barriers
     */
    private SequenceBarrier sequenceBarrier1;
    private SequenceBarrier sequenceBarrier2;
    private SequenceBarrier sequenceBarrier3;
    private SequenceBarrier sequenceBarrier4;
    private SequenceBarrier sequenceBarrier12;
    private SequenceBarrier sequenceBarrier34;
    private SequenceBarrier sequenceBarrier1234;

    private FourStreamAggregator(int vtpPort1, int vtpPort2, int vtpPort3,
                                   int vtpPort4) {
        this.vtpPort1 = vtpPort1;
        this.vtpPort2 = vtpPort2;
        this.vtpPort3 = vtpPort3;
        this.vtpPort4 = vtpPort4;

        ringBuffer1 = createSingleProducer(new RingEventFactory(), maxRingItems,
                new LiteBlockingWaitStrategy());
        sequence1 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier1 = ringBuffer1.newBarrier();
        ringBuffer1.addGatingSequences(sequence1);

        ringBuffer2 = createSingleProducer(new RingEventFactory(), maxRingItems,
                new LiteBlockingWaitStrategy());
        sequence2 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier2 = ringBuffer2.newBarrier();
        ringBuffer2.addGatingSequences(sequence2);

        ringBuffer3 = createSingleProducer(new RingEventFactory(), maxRingItems,
                new LiteBlockingWaitStrategy());
        sequence3 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier3 = ringBuffer3.newBarrier();
        ringBuffer3.addGatingSequences(sequence3);

        ringBuffer4 = createSingleProducer(new RingEventFactory(), maxRingItems,
                new LiteBlockingWaitStrategy());
        sequence4 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier4 = ringBuffer4.newBarrier();
        ringBuffer4.addGatingSequences(sequence4);

        ringBuffer12 = createSingleProducer(new RingEventFactory(), maxRingItems,
                new LiteBlockingWaitStrategy());
        sequence12 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier12 = ringBuffer12.newBarrier();
        ringBuffer12.addGatingSequences(sequence12);

        ringBuffer34 = createSingleProducer(new RingEventFactory(), maxRingItems,
                new LiteBlockingWaitStrategy());
        sequence34 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier34 = ringBuffer34.newBarrier();
        ringBuffer34.addGatingSequences(sequence34);

        ringBuffer1234 = createSingleProducer(new RingEventFactory(), maxRingItems,
                new LiteBlockingWaitStrategy());
        sequence1234 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier1234 = ringBuffer1234.newBarrier();
        ringBuffer1234.addGatingSequences(sequence1234);

    }

    private void go() {
        Receiver receiver1 = new Receiver(vtpPort1, 1, ringBuffer1, 10);
        Receiver receiver2 = new Receiver(vtpPort2, 2, ringBuffer2, 10);
        Receiver receiver3 = new Receiver(vtpPort3, 3, ringBuffer3, 10);
        Receiver receiver4 = new Receiver(vtpPort4, 4, ringBuffer4, 10);

        Aggregator aggregator12 = new Aggregator(ringBuffer1, ringBuffer2, sequence1,
                sequence2, sequenceBarrier1, sequenceBarrier2, ringBuffer12);

        Aggregator aggregator34 = new Aggregator(ringBuffer3, ringBuffer4, sequence3,
                sequence4, sequenceBarrier3, sequenceBarrier4, ringBuffer34);

        Aggregator aggregator1234 = new Aggregator(ringBuffer12, ringBuffer34, sequence12,
                sequence34, sequenceBarrier12, sequenceBarrier34, ringBuffer1234);

        Consumer consumer = new Consumer(ringBuffer1234, sequence1234, sequenceBarrier1234, 0);

        receiver1.start();
        receiver2.start();
        receiver3.start();
        receiver4.start();

        aggregator12.start();
        aggregator34.start();
        aggregator1234.start();

        consumer.start();
    }

    public static void main(String[] args) {
        int port1 = Integer.parseInt(args[0]);
        int port2 = Integer.parseInt(args[1]);
        int port3 = Integer.parseInt(args[2]);
        int port4 = Integer.parseInt(args[3]);

        new FourStreamAggregator(port1, port2, port3, port4).go();
    }

}
