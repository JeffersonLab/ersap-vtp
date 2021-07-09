package org.jlab.epsci.stream.sampaBB;

import com.lmax.disruptor.*;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;

public class SMPTwoStreamAggregatorDecoder {
    /**
     * SAMPA ports and stream info
     */
    private final int sampaPort1;
    private final int sampaPort2;
    private int streamId1;
    private int streamId2;
    private int streamFrameLimit;

    /**
     * Max ring items
     */
    private final static int maxRingItems = 64;

    /**
     * Ring buffers
     */
    private final RingBuffer<SRingRawEvent> ringBuffer1;
    private final RingBuffer<SRingRawEvent> ringBuffer2;
    private final RingBuffer<SRingRawEvent> ringBuffer12;

    /**
     * Sequences
     */
    private final Sequence sequence1;
    private final Sequence sequence2;
    private final Sequence sequence12;

    /**
     * Sequence barriers
     */
    private final SequenceBarrier sequenceBarrier1;
    private final SequenceBarrier sequenceBarrier2;
    private final SequenceBarrier sequenceBarrier12;

    private SReceiver receiver1;
    private SReceiver receiver2;
    private SAggregator aggregator12;
    private SConsumer consumer;

    public SMPTwoStreamAggregatorDecoder(int sampaPort1, int sampaPort2,
                                         int streamId1, int streamId2,
                                         int streamFrameLimit) {
        this.sampaPort1 = sampaPort1;
        this.sampaPort2 = sampaPort2;
        this.streamId1 = streamId1;
        this.streamId2 = streamId2;
        this.streamFrameLimit = streamFrameLimit;

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

        ringBuffer12 = createSingleProducer(new SRingRawEventFactory(), maxRingItems,
                new YieldingWaitStrategy());
        sequence12 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier12 = ringBuffer12.newBarrier();
        ringBuffer12.addGatingSequences(sequence12);

    }

    public void go() {
        receiver1 = new SReceiver(sampaPort1, streamId1, ringBuffer1, streamFrameLimit);
        receiver2 = new SReceiver(sampaPort2, streamId2, ringBuffer2, streamFrameLimit);

        aggregator12 = new SAggregator(ringBuffer1, ringBuffer2, sequence1,
                sequence2, sequenceBarrier1, sequenceBarrier2, ringBuffer12);

        consumer = new SConsumer(ringBuffer12, sequence12, sequenceBarrier12);
        receiver1.start();
        receiver2.start();

        aggregator12.start();
        consumer.start();
    }

    public void close() {
        receiver1.exit();
        receiver2.exit();
        aggregator12.exit();
        consumer.exit();
    }

    public static void main(String[] args) {
        int port1 = Integer.parseInt(args[0]);
        int port2 = Integer.parseInt(args[1]);

        int streamId1 = Integer.parseInt(args[2]);
        int streamId2 = Integer.parseInt(args[3]);

        int streamFrameLimit = Integer.parseInt(args[4]);

        new SMPTwoStreamAggregatorDecoder(port1, port2, streamId1, streamId2,streamFrameLimit ).go();
    }
}
