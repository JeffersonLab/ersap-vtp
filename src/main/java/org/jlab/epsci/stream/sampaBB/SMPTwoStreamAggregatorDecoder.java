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

import java.io.IOException;

import static com.lmax.disruptor.RingBuffer.createSingleProducer;


public class SMPTwoStreamAggregatorDecoder {
    /**
     * SAMPA ports and stream info
     */
    private final int sampaPort1;
    private final int sampaPort2;
    private final int streamId1;
    private final int streamId2;
    private final int streamFrameLimit;

    /** Format of data SAMPA chips are sending. */
    private final SampaType sampaType;

    /**
     * Max ring items
     */
    private final static int maxRingItems = 16;

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
                                         int streamFrameLimit,
                                         SampaType sampaType) {
        this.sampaPort1 = sampaPort1;
        this.sampaPort2 = sampaPort2;
        this.streamId1 = streamId1;
        this.streamId2 = streamId2;
        this.streamFrameLimit = streamFrameLimit;
        this.sampaType = sampaType;

        // RingBuffer in which receiver1 will get & fill events, then pass them to the aggregator
        ringBuffer1 = createSingleProducer(new SRingRawEventFactory(sampaType), maxRingItems,
                new SpinCountBackoffWaitStrategy(30000, new LiteBlockingWaitStrategy()));

        sequence1 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier1 = ringBuffer1.newBarrier();
        ringBuffer1.addGatingSequences(sequence1);

        // RingBuffer in which receiver2 will get & fill events, then pass them to the aggregator
        ringBuffer2 = createSingleProducer(new SRingRawEventFactory(sampaType), maxRingItems,
                new SpinCountBackoffWaitStrategy(30000, new LiteBlockingWaitStrategy()));

        sequence2 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier2 = ringBuffer2.newBarrier();
        ringBuffer2.addGatingSequences(sequence2);

        // RingBuffer in which Aggregator will get empty events and fill them with data aggregated
        // from the 2 streams. It then passes them to the consumer.
        ringBuffer12 = createSingleProducer(new SRingRawEventFactory(sampaType), maxRingItems,
                new SpinCountBackoffWaitStrategy(30000, new LiteBlockingWaitStrategy()));

        sequence12 = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
        sequenceBarrier12 = ringBuffer12.newBarrier();
        ringBuffer12.addGatingSequences(sequence12);

    }

    public void go() throws IOException {
        receiver1 = new SReceiver(sampaPort1, streamId1, ringBuffer1, streamFrameLimit, sampaType);
        receiver2 = new SReceiver(sampaPort2, streamId2, ringBuffer2, streamFrameLimit, sampaType);

        aggregator12 = new SAggregator(ringBuffer1, ringBuffer2,
                                       sequence1, sequence2,
                                       sequenceBarrier1, sequenceBarrier2,
                                       ringBuffer12, sampaType);

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

    /**
     * Main method. Arguments are:
     * <ol>
     * <li>port of TCP server to run in first SReceiver object
     * <li>port of TCP server to run in second SReceiver object
     * <li>id of first SReceiver's data stream
     * <li>id of second SReceiver's data stream
     * <li>limit on number of frames to parse on each stream
     * <li>optional: if = DAS, it switches from parsing DSP format to DAS format data
     * </ol>
     * @param args array of args.
     */
    public static void main(String[] args) {
        int port1 = Integer.parseInt(args[0]);
        int port2 = Integer.parseInt(args[1]);

        int streamId1 = Integer.parseInt(args[2]);
        int streamId2 = Integer.parseInt(args[3]);

        int streamFrameLimit = Integer.parseInt(args[4]);

        SampaType sampaType = SampaType.DSP;
        if (args.length > 5) {
            String sType = args[5];
            if (sType.equalsIgnoreCase("das")) {
                sampaType = SampaType.DAS;
            }
        }

        try {
            new SMPTwoStreamAggregatorDecoder(port1, port2, streamId1, streamId2,
                                              streamFrameLimit, sampaType).go();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
