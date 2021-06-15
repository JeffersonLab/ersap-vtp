package org.jlab.epsci.stream.vtp;

import com.lmax.disruptor.*;

import java.nio.ByteBuffer;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;

public class VTPTwoStreamAggregatorDecoder {

    /**
     * VTP ports
     */
    private int vtpPort1;
    private int vtpPort2;

    /**
     * Max ring items
     */
    private final static int maxRingItems = 32768;

    /**
     * Ring buffers
     */
    private RingBuffer<VRingRawEvent> ringBuffer1;
    private RingBuffer<VRingRawEvent> ringBuffer2;
    private RingBuffer<VRingRawEvent> ringBuffer12;

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

    private VReceiver receiver1;
    private VReceiver receiver2;
    private VAggregator aggregator12;
    private VConsumer consumer;

    private boolean started = false;

    public VTPTwoStreamAggregatorDecoder(int vtpPort1, int vtpPort2) {
        this.vtpPort1 = vtpPort1;
        this.vtpPort2 = vtpPort2;

        ringBuffer1 = createSingleProducer(new VRingRawEventFactory(), maxRingItems,
//                new LiteBlockingWaitStrategy());
                new YieldingWaitStrategy());
//                new SpinCountBackoffWaitStrategy(30000, new LiteBlockingWaitStrategy()));
        sequence1 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier1 = ringBuffer1.newBarrier();
        ringBuffer1.addGatingSequences(sequence1);

        ringBuffer2 = createSingleProducer(new VRingRawEventFactory(), maxRingItems,
                new YieldingWaitStrategy());
        sequence2 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier2 = ringBuffer2.newBarrier();
        ringBuffer2.addGatingSequences(sequence2);


        ringBuffer12 = createSingleProducer(new VRingRawEventFactory(), maxRingItems,
                new YieldingWaitStrategy());
        sequence12 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier12 = ringBuffer12.newBarrier();
        ringBuffer12.addGatingSequences(sequence12);

    }

    public void go() {
        if(!started) {
            receiver1 = new VReceiver(vtpPort1, 1, ringBuffer1, 10);
            receiver2 = new VReceiver(vtpPort2, 2, ringBuffer2, 10);

            aggregator12 = new VAggregator(ringBuffer1, ringBuffer2, sequence1,
                    sequence2, sequenceBarrier1, sequenceBarrier2, ringBuffer12);

            int runNumber = 0;
            consumer = new VConsumer(ringBuffer12, sequence12, sequenceBarrier12, runNumber);

            receiver1.start();
            receiver2.start();

            aggregator12.start();
            consumer.start();
            started = true;
        }
    }

    public ByteBuffer getDecodedEvent() throws Exception {
        return consumer.getEvent();
    }

    public void close(){
        receiver1.exit();
        receiver2.exit();
        aggregator12.exit();
        consumer.exit();
        started = false;
    }

    public static void main(String[] args) {
        int port1 = Integer.parseInt(args[0]);
        int port2 = Integer.parseInt(args[1]);

        new VTPTwoStreamAggregatorDecoder(port1, port2).go();
    }

}
