package org.jlab.epsci.ersap.vtp;

import com.lmax.disruptor.*;

import java.util.HashMap;


/**
 * __
 * /  \
 * \  /     ___       __
 * --  --> |   |     /  \
 * __      |   |-->  \  /
 * /  \ --> ---       --
 * \  /
 * --
 */
public class Aggregator extends Thread {

    /**
     * Maps for aggregation
     */
//    private HashMap<BigInteger, byte[]> m1 = new HashMap<>();
//    private HashMap<BigInteger, byte[]> m2 = new HashMap<>();
    private final HashMap<Long, byte[]> m1 = new HashMap<>();
    private final HashMap<Long, byte[]> m2 = new HashMap<>();

    /**
     * Current spot in output ring from which an item was claimed.
     */
    private long outSequence;


    /**
     * 1 RingBuffer per stream.
     */
    private RingBuffer<RingEvent> ringBuffer1;
    private RingBuffer<RingEvent> ringBuffer2;

    /**
     * 1 sequence per stream
     */
    private Sequence sequence1;
    private Sequence sequence2;

    /**
     * 1 barrier per stream
     */
    private SequenceBarrier barrier1;
    private SequenceBarrier barrier2;

    /**
     * Track which sequence the aggregating consumer wants next from each of the crate rings.
     */
    private long nextSequence1;
    private long nextSequence2;

    /**
     * Track which sequence is currently available from each of the crate rings.
     */
    private long availableSequence1;
    private long availableSequence2;

    /**
     * 1 output RingBuffer.
     */
    private RingBuffer<RingEvent> outputRingBuffer;


    public Aggregator(RingBuffer<RingEvent> ringBuffer1, RingBuffer<RingEvent> ringBuffer2,
                      Sequence sequence1, Sequence sequence2,
                      SequenceBarrier barrier1, SequenceBarrier barrier2,
                      RingBuffer<RingEvent> outputRingBuffer
    ) {

        this.ringBuffer1 = ringBuffer1;
        this.ringBuffer2 = ringBuffer2;
        this.sequence1 = sequence1;
        this.sequence2 = sequence2;
        this.barrier1 = barrier1;
        this.barrier2 = barrier2;
        this.outputRingBuffer = outputRingBuffer;

//        ringBuffer1.addGatingSequences(sequence1);
//        ringBuffer2.addGatingSequences(sequence2);

        nextSequence1 = sequence1.get() + 1L;
        nextSequence2 = sequence2.get() + 1L;

        availableSequence1 = -1L;
        availableSequence2 = -1L;
    }


    public void get() throws InterruptedException {

        try {
            if (availableSequence1 < nextSequence1) {
                availableSequence1 = barrier1.waitFor(nextSequence1);
            }
            RingEvent inputItem1 = ringBuffer1.get(nextSequence1);

            if (availableSequence2 < nextSequence2) {
                availableSequence2 = barrier2.waitFor(nextSequence2);
            }
            RingEvent inputItem2 = ringBuffer2.get(nextSequence2);

//            BigInteger b1 = inputItem1.getRecordNumber();
//            BigInteger b2 = inputItem2.getRecordNumber();
            Long b1 = inputItem1.getRecordNumber();
            Long b2 = inputItem2.getRecordNumber();

            int l1 = inputItem1.getPayloadDataLength();
            int l2 = inputItem2.getPayloadDataLength();
            /*
            // ...........................................//
            // we are lucky we found a match
            if (b1 == b2) {
                // get the next available sequence on the output ring
                outSequence = outputRingBuffer.next();

                // get the the event at that sequence
                RingEvent outputItem = outputRingBuffer.get(outSequence);
                // clear the bytebuffer of that event
                outputItem.getPayloadBuffer().clear();

                // see if the byte[] is big enough to hold the aggregated payload
                if (outputItem.getPayload().length < (l1 + l2) ){
                    byte [] aggregate = new byte[l1 + l2];
                    outputItem.setPayload(aggregate);
                }
                // set the payload length of the output event
                outputItem.setPayloadDataLength(l1 + l2);
                // aggregate payload and set the payload [] in the output event
                // now the output event byteBuffer will wrap the payload[]
                EUtil.addByteArrays(inputItem1.getPayload(), l1, inputItem2.getPayload(), l2, outputItem.getPayload());
                // sat the matching record number. does not matter b1 or b2
                outputItem.setRecordNumber(b1);

            } else if (b1 > b2) {
                // read the next in the second ring until b2 >= b1
                // XXX
                if (b1 == b2) {
                    // we found the match
                } else if (b1 < b2) {
                    // b2 frame was dropped at the FPGA
                    // read the next in the first ring until b1 >= b2
                    if( b1 == b2) {
                        // we found the match
                    } else if (b1 > b2) {
                        //got to XXX
                    }
                }

            } else if (b2 > b1){
               // read the next in the first ring until b1 >= b2
               /// YYY
               if (b2 == b1) {
                   // we found the match
               } else if (b2 < b1) {
                   // b1 frame was dropped at the FPGA
                   // read the next in the second ring until b2 >= b1
                   if(b2 ==b1) {
                       // we found the match
                   } else if (b2 > b1) {
                       // go to YYY
                   }
               }
            }
            // ...........................................//
*/
            m1.put(b1, inputItem1.getPayload());
            m2.put(b2, inputItem2.getPayload());

//            BigInteger aggRecNum = null;

            if (m1.containsKey(b1) && m2.containsKey(b1)) {
                outSequence = outputRingBuffer.next();
                RingEvent outputItem = outputRingBuffer.get(outSequence);
                outputItem.getPayloadBuffer().clear();

                if (outputItem.getPayload().length < (l1 + l2)) {
                    byte[] aggregate = new byte[l1 + l2];
                    outputItem.setPayload(aggregate);
                    outputItem.setPayloadDataLength(l1 + l2);
                }

                EUtil.addByteArrays(m1.get(b1), l1, m2.get(b1), l2, outputItem.getPayload());
                outputItem.setRecordNumber(b1);
                outputRingBuffer.publish(outSequence);
                m1.remove(b1);
                m2.remove(b1);
            }

            if (m1.containsKey(b2) && m2.containsKey(b2)) {

                outSequence = outputRingBuffer.next();
                RingEvent outputItem = outputRingBuffer.get(outSequence);
                outputItem.getPayloadBuffer().clear();

                if (outputItem.getPayload().length < (l1 + l2)) {
                    byte[] aggregate = new byte[l1 + l2];
                    outputItem.setPayload(aggregate);
                    outputItem.setPayloadDataLength(l1 + l2);
                }

                EUtil.addByteArrays(m1.get(b2), l1, m2.get(b2), l2, outputItem.getPayload());
                outputItem.setRecordNumber(b2);
                outputRingBuffer.publish(outSequence);
                m1.remove(b2);
                m2.remove(b2);
            }
        } catch (final TimeoutException | AlertException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * This "consumer" is also a producer for the output ring.
     * So get items from the output ring and fill them with items claimed from the input rings.
     */
    private void put() throws InterruptedException {


        sequence1.set(nextSequence1);
        nextSequence1++;

        sequence2.set(nextSequence2);
        nextSequence2++;

    }

    public void run() {
        try {
            while (true) {
                get();
                put();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
