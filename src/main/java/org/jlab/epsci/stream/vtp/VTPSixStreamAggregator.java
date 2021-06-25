package org.jlab.epsci.stream.vtp;

import com.lmax.disruptor.*;
import static com.lmax.disruptor.RingBuffer.createSingleProducer;

public class VTPSixStreamAggregator {

    /**
     * VTP ports
     */
    private int vtpPort1;
    private int vtpPort2;
    private int vtpPort3;
    private int vtpPort4;
    private int vtpPort5;
    private int vtpPort6;

    /**
     * Max ring items
     */
    private final static int maxRingItems = 1024;

    /**
     * Ring buffers
     */
    private RingBuffer<VRingRawEvent> ringBuffer1;
    private RingBuffer<VRingRawEvent> ringBuffer2;
    private RingBuffer<VRingRawEvent> ringBuffer3;
    private RingBuffer<VRingRawEvent> ringBuffer4;
    private RingBuffer<VRingRawEvent> ringBuffer5;
    private RingBuffer<VRingRawEvent> ringBuffer6;
    private RingBuffer<VRingRawEvent> ringBuffer12;
    private RingBuffer<VRingRawEvent> ringBuffer34;
    private RingBuffer<VRingRawEvent> ringBuffer56;
    private RingBuffer<VRingRawEvent> ringBuffer1234;
    private RingBuffer<VRingRawEvent> ringBuffer123456;

    /**
     * Sequences
     */
    private Sequence sequence1;
    private Sequence sequence2;
    private Sequence sequence3;
    private Sequence sequence4;
    private Sequence sequence5;
    private Sequence sequence6;
    private Sequence sequence12;
    private Sequence sequence34;
    private Sequence sequence56;
    private Sequence sequence1234;
    private Sequence sequence123456;

    /**
     * Sequence barriers
     */
    private SequenceBarrier sequenceBarrier1;
    private SequenceBarrier sequenceBarrier2;
    private SequenceBarrier sequenceBarrier3;
    private SequenceBarrier sequenceBarrier4;
    private SequenceBarrier sequenceBarrier5;
    private SequenceBarrier sequenceBarrier6;
    private SequenceBarrier sequenceBarrier12;
    private SequenceBarrier sequenceBarrier34;
    private SequenceBarrier sequenceBarrier56;
    private SequenceBarrier sequenceBarrier1234;
    private SequenceBarrier sequenceBarrier123456;

    private int runNumber;

    private VTPSixStreamAggregator(int vtpPort1, int vtpPort2, int vtpPort3,
                                   int vtpPort4, int vtpPort5, int vtpPort6,
                                   int runNumber) {
        this.vtpPort1 = vtpPort1;
        this.vtpPort2 = vtpPort2;
        this.vtpPort3 = vtpPort3;
        this.vtpPort4 = vtpPort4;
        this.vtpPort5 = vtpPort5;
        this.vtpPort6 = vtpPort6;

        this.runNumber = runNumber;

        ringBuffer1 = createSingleProducer(new VRingRawEventFactory(), maxRingItems,
                new LiteBlockingWaitStrategy());
        sequence1 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier1 = ringBuffer1.newBarrier();
        ringBuffer1.addGatingSequences(sequence1);

        ringBuffer2 = createSingleProducer(new VRingRawEventFactory(), maxRingItems,
                new LiteBlockingWaitStrategy());
        sequence2 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier2 = ringBuffer2.newBarrier();
        ringBuffer2.addGatingSequences(sequence2);

        ringBuffer3 = createSingleProducer(new VRingRawEventFactory(), maxRingItems,
                new LiteBlockingWaitStrategy());
        sequence3 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier3 = ringBuffer3.newBarrier();
        ringBuffer3.addGatingSequences(sequence3);

        ringBuffer4 = createSingleProducer(new VRingRawEventFactory(), maxRingItems,
                new LiteBlockingWaitStrategy());
        sequence4 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier4 = ringBuffer4.newBarrier();
        ringBuffer4.addGatingSequences(sequence4);

        ringBuffer5 = createSingleProducer(new VRingRawEventFactory(), maxRingItems,
                new LiteBlockingWaitStrategy());
        sequence5 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier5 = ringBuffer5.newBarrier();
        ringBuffer5.addGatingSequences(sequence5);

        ringBuffer6 = createSingleProducer(new VRingRawEventFactory(), maxRingItems,
                new LiteBlockingWaitStrategy());
        sequence6 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier6 = ringBuffer6.newBarrier();
        ringBuffer6.addGatingSequences(sequence6);

        ringBuffer12 = createSingleProducer(new VRingRawEventFactory(), maxRingItems,
                new LiteBlockingWaitStrategy());
        sequence12 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier12 = ringBuffer12.newBarrier();
        ringBuffer12.addGatingSequences(sequence12);

        ringBuffer34 = createSingleProducer(new VRingRawEventFactory(), maxRingItems,
                new LiteBlockingWaitStrategy());
        sequence34 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier34 = ringBuffer34.newBarrier();
        ringBuffer34.addGatingSequences(sequence34);

        ringBuffer56 = createSingleProducer(new VRingRawEventFactory(), maxRingItems,
                new LiteBlockingWaitStrategy());
        sequence56 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier56 = ringBuffer56.newBarrier();
        ringBuffer56.addGatingSequences(sequence56);

        ringBuffer1234 = createSingleProducer(new VRingRawEventFactory(), maxRingItems,
                new LiteBlockingWaitStrategy());
        sequence1234 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier1234 = ringBuffer1234.newBarrier();
        ringBuffer1234.addGatingSequences(sequence1234);

        ringBuffer123456 = createSingleProducer(new VRingRawEventFactory(), maxRingItems,
                new LiteBlockingWaitStrategy());
        sequence123456 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier123456 = ringBuffer123456.newBarrier();
        ringBuffer123456.addGatingSequences(sequence123456);

    }

    private void go() {
        VReceiver receiver1 = new VReceiver(vtpPort1, 1, ringBuffer1, 10);
        VReceiver receiver2 = new VReceiver(vtpPort2, 2, ringBuffer2, 10);
        VReceiver receiver3 = new VReceiver(vtpPort3, 3, ringBuffer3, 10);
        VReceiver receiver4 = new VReceiver(vtpPort4, 4, ringBuffer4, 10);
        VReceiver receiver5 = new VReceiver(vtpPort5, 5, ringBuffer5, 10);
        VReceiver receiver6 = new VReceiver(vtpPort6, 6, ringBuffer6, 10);

        VAggregator aggregator12 = new VAggregator(ringBuffer1, ringBuffer2, sequence1,
                sequence2, sequenceBarrier1, sequenceBarrier2, ringBuffer12);

        VAggregator aggregator34 = new VAggregator(ringBuffer3, ringBuffer4, sequence3,
                sequence4, sequenceBarrier3, sequenceBarrier4, ringBuffer34);

        VAggregator aggregator56 = new VAggregator(ringBuffer5, ringBuffer6, sequence5,
                sequence6, sequenceBarrier5, sequenceBarrier6, ringBuffer56);

        VAggregator aggregator1234 = new VAggregator(ringBuffer12, ringBuffer34, sequence12,
                sequence34, sequenceBarrier12, sequenceBarrier34, ringBuffer1234);

        VAggregator aggregator123456 = new VAggregator(ringBuffer1234, ringBuffer56, sequence1234,
                sequence56, sequenceBarrier1234, sequenceBarrier56, ringBuffer123456);

        VConsumer consumer = new VConsumer(ringBuffer123456, sequence123456, sequenceBarrier123456, runNumber);

        receiver1.start();
        receiver2.start();
        receiver3.start();
        receiver4.start();
        receiver5.start();
        receiver6.start();

        aggregator12.start();
        aggregator34.start();
        aggregator56.start();
        aggregator1234.start();
        aggregator123456.start();

        consumer.start();

    }

    public static void main(String[] args) {
        int port1 = Integer.parseInt(args[0]);
        int port2 = Integer.parseInt(args[1]);
        int port3 = Integer.parseInt(args[2]);
        int port4 = Integer.parseInt(args[3]);
        int port5 = Integer.parseInt(args[4]);
        int port6 = Integer.parseInt(args[5]);
        int run_number = Integer.parseInt(args[6]);

        new VTPSixStreamAggregator(port1, port2, port3, port4, port5, port6, run_number).go();
    }

}
