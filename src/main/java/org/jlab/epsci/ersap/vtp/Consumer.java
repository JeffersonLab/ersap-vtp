package org.jlab.epsci.ersap.vtp;

import com.lmax.disruptor.*;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static org.jlab.epsci.ersap.vtp.EUtil.printHits;

public class Consumer extends Thread {
    private RingBuffer<RingEvent> ringBuffer;
    private Sequence sequence;
    private SequenceBarrier barrier;

    private long nextSequence;
    private long availableSequence;

    /**
     * Consumer constructor
     *
     * @param ringBuffer
     * @param sequence
     * @param barrier
     * @param runNumber
     */
    public Consumer(RingBuffer<RingEvent> ringBuffer,
                    Sequence sequence,
                    SequenceBarrier barrier,
                    int runNumber) {

        this.ringBuffer = ringBuffer;
        this.sequence = sequence;
        this.barrier = barrier;

        ringBuffer.addGatingSequences(sequence);
        nextSequence = sequence.get() + 1L;
        availableSequence = -1L;

    }

    /**
     * Get the next available item from outupt ring buffer.
     * Do NOT call this multiple times in a row!
     * Be sure to call "put" before calling this again.
     *
     * @return next available item in ring buffer.
     * @throws InterruptedException
     */
    public RingEvent get() throws InterruptedException {

        RingEvent item = null;

        try {
            if (availableSequence < nextSequence) {
                availableSequence = barrier.waitFor(nextSequence);
            }

            item = ringBuffer.get(nextSequence);
        } catch (final TimeoutException | AlertException ex) {
            // never happen since we don't use timeout wait strategy
            ex.printStackTrace();
        }

        return item;
    }

    private List<AdcHit> decodePayload(BigInteger frame_time_ns, byte[] payload) {
        List<AdcHit> res = new ArrayList<>();
        ByteBuffer bb = ByteBuffer.wrap(payload);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        int[] slot_ind = new int[8];
        int[] slot_len = new int[8];
        long tag = EUtil.getUnsignedInt(bb);
        System.out.println(String.format("DDD:tag = %x", tag));
        if ((tag & 0x8FFF8000L) == 0x80000000L) {

            for (int jj = 0; jj < 8; jj++) {
                slot_ind[jj] = EUtil.getUnsignedShort(bb);
                slot_len[jj] = EUtil.getUnsignedShort(bb);
            }
            for (int i = 0; i < 8; i++) {
                if (slot_len[i] > 0) {
                    bb.position(slot_ind[i] * 4);
                    int type = 0;
                    for (int j = 0; j < slot_len[i]; j++) {
                        int val = bb.getInt();
                        AdcHit hit = new AdcHit();

                        if ((val & 0x80000000) == 0x80000000) {
                            type = (val >> 15) & 0xFFFF;
                            hit.setCrate((val >> 8) & 0x007F);
                            hit.setSlot((val) & 0x001F);
                        } else if (type == 0x0001) /* FADC hit type */ {
                            hit.setQ((val) & 0x1FFF);
                            hit.setChannel((val >> 13) & 0x000F);
                            long v = ((val >> 17) & 0x3FFF) * 4;
                            BigInteger ht = BigInteger.valueOf(v);
                            hit.setTime(frame_time_ns.add(ht));
                            hit.setTime(ht);
                            res.add(hit);
                        }
                    }
                }
            }
        } else {
            System.out.println("parser error: wrong tag");
            System.exit(0);
        }
        return res;
    }

    /**
     * This "consumer" is also a producer for the output ring.
     * So get items from the output ring and fill them with items claimed from the input rings.
     */
    public void put() throws InterruptedException {

        // Tell input (crate) ring that we're done with the item we're consuming
        sequence.set(nextSequence);

        // Go to next item to consume on input ring
        nextSequence++;
    }

    public void run() {
        HitFinder hitFinder = new HitFinder();

        try {

            while (true) {
                // Get an empty item from ring
                RingEvent buf = get();

                BigInteger frameTime =
                        buf.getRecordNumber().multiply(EUtil.toUnsignedBigInteger(65536L));
                byte[] payload = buf.getPayload();
                if (payload.length > 0) {
//                    List<AdcHit> evt = decodePayload(frameTime, payload);
//                    Map<Integer, List<ChargeTime>> hits = hitFinder
//                            .reset()
//                            .stream(evt)
//                            .frameStartTime(frameTime)
//                            .frameLength(64000)
//                            .sliceSize(32)
//                            .windowSize(4)
//                            .slide();
//                    printHits(hits);
                }

                put();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
