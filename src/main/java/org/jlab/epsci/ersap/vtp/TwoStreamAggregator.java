package org.jlab.epsci.ersap.vtp;

import com.lmax.disruptor.*;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;

public class TwoStreamAggregator {

    /**
     * VTP ports
     */
    private int vtpPort1;
    private int vtpPort2;

    /**
     * Max ring items
     */
    private final static int maxRingItems = 8192;

    /**
     * Ring buffers
     */
    private RingBuffer<RingRawEvent> ringBuffer1;
    private RingBuffer<RingRawEvent> ringBuffer2;
    private RingBuffer<RingRawEvent> ringBuffer12;

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

    private Receiver receiver1;
    private Receiver receiver2;
    private Aggregator aggregator12;
    private Consumer consumer;

    public TwoStreamAggregator(int vtpPort1, int vtpPort2) {
        this.vtpPort1 = vtpPort1;
        this.vtpPort2 = vtpPort2;

        ringBuffer1 = createSingleProducer(new RingRawEventFactory(), maxRingItems,
//                new LiteBlockingWaitStrategy());
                new YieldingWaitStrategy());
//                new SpinCountBackoffWaitStrategy(30000, new LiteBlockingWaitStrategy()));
        sequence1 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier1 = ringBuffer1.newBarrier();
        ringBuffer1.addGatingSequences(sequence1);

        ringBuffer2 = createSingleProducer(new RingRawEventFactory(), maxRingItems,
                new YieldingWaitStrategy());
        sequence2 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier2 = ringBuffer2.newBarrier();
        ringBuffer2.addGatingSequences(sequence2);


        ringBuffer12 = createSingleProducer(new RingRawEventFactory(), maxRingItems,
                new YieldingWaitStrategy());
        sequence12 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier12 = ringBuffer12.newBarrier();
        ringBuffer12.addGatingSequences(sequence12);

    }

    public void go() {
        receiver1 = new Receiver(vtpPort1, 1, ringBuffer1, 10);
        receiver2 = new Receiver(vtpPort2, 2, ringBuffer2, 10);

        aggregator12 = new Aggregator(ringBuffer1, ringBuffer2, sequence1,
                sequence2, sequenceBarrier1, sequenceBarrier2, ringBuffer12);

        int runNumber = 0;
        consumer = new Consumer(ringBuffer12, sequence12, sequenceBarrier12, runNumber);

        receiver1.start();
        receiver2.start();

        aggregator12.start();
        consumer.start();

    }

    public void close(){
        receiver1.exit();
        receiver2.exit();
        aggregator12.exit();
        consumer.exit();
    }

    public static void main(String[] args) {
        int port1 = Integer.parseInt(args[0]);
        int port2 = Integer.parseInt(args[1]);

        new TwoStreamAggregator(port1, port2).go();
    }

}
