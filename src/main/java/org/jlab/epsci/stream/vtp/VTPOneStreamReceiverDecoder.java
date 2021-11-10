package org.jlab.epsci.stream.vtp;

import com.lmax.disruptor.*;

import java.nio.ByteBuffer;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;

public class VTPOneStreamReceiverDecoder {

    /**
     * VTP port
     */
    private int vtpPort1;

    /**
     * Max ring items
     */
//    private final static int maxRingItems = 32768;
    private final static int maxRingItems = 64;

    /**
     * Ring buffer
     */
    private RingBuffer<VRingRawEvent> ringBuffer1;

    /**
     * Sequences
     */
    private Sequence sequence1;

    /**
     * Sequence barriers
     */
    private SequenceBarrier sequenceBarrier1;

    private VReceiver receiver1;
    private VConsumer consumer;

    private boolean started = false;

    public VTPOneStreamReceiverDecoder(int vtpPort1) {
        this.vtpPort1 = vtpPort1;

        ringBuffer1 = createSingleProducer(new VRingRawEventFactory(), maxRingItems,
                new YieldingWaitStrategy());
        sequence1 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier1 = ringBuffer1.newBarrier();
        ringBuffer1.addGatingSequences(sequence1);

    }

    public void go() {
        if(!started) {
            receiver1 = new VReceiver(vtpPort1, 1, ringBuffer1, 10);
            int runNumber = 0;
            consumer = new VConsumer(ringBuffer1, sequence1, sequenceBarrier1, runNumber);

            receiver1.start();
            consumer.start();
            started = true;
        }
    }

    public ByteBuffer getDecodedEvent() throws Exception {
        return consumer.getEvent();
    }

    public void close(){
        receiver1.exit();
        consumer.exit();
        started = false;
    }

    public static void main(String[] args) {
        int port1 = Integer.parseInt(args[0]);
        new VTPOneStreamReceiverDecoder(port1).go();
    }

}
