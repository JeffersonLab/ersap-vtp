package org.jlab.epsci.ersap.vtp;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HitFinder {

    private BigInteger frameStartTime_ns = BigInteger.ZERO;
    private int frameLength_ns = 64000;
    private int slice_ns = 32;
    private int slidingWindowSize = 3;

    private static Map<Integer, List<Integer>> slideCharges = new HashMap<>();
    private static Map<Integer, List<ChargeTime>> hits = new HashMap<>();
    private List<AdcHit> vtpStream;

    HitFinder reset() {
        slideCharges.clear();
        hits.clear();
        if (vtpStream != null && !vtpStream.isEmpty()) {
            vtpStream.clear();
        }
        return this;
    }

    public HitFinder stream(List<AdcHit> stream) {
        this.vtpStream = stream;
        return this;
    }

    HitFinder frameStartTime(BigInteger frameStartTime) {
        this.frameStartTime_ns = frameStartTime;
        return this;
    }

    HitFinder frameLength(int frameLength) {
        this.frameLength_ns = frameLength;
        return this;
    }

    HitFinder sliceSize(int slice) {
        this.slice_ns = slice;
        return this;
    }

    HitFinder windowSize(int slide) {
        this.slidingWindowSize = slide;
        return this;
    }

    public BigInteger getFrameStartTime_ns() {
        return frameStartTime_ns;
    }

    Map<Integer, List<ChargeTime>> slide() {
//        _slide(getSlices(vtpStream), slidingWindowSize);
//        findHits();
        return hits;
    }

    /**
     * Slicing vtp stream frame with a time window defined by the VTP time stamps.
     * This will use a stream of {@link AdcHit} elements supporting sequential
     * and parallel aggregate operations. First it will filter those hits
     * that have a time consistent with the window. After we group elements,
     * classifying them by crate, slot, channel and then performing a reduction
     * operation on the values associated with the crate-slot-channel using the
     * specified downstream collector.
     *
     * @param leading window start time
     * @param trailing window end time
     * @param vtpStream aggregated VTP stream
     * @return VTP stream single frame. Map, where key: encoded int = crate.slot,channel,
     * value: sum of charge reported for the key
     */
    private Map<Integer, Integer> streamSlice(BigInteger leading, BigInteger trailing,
                                              List<AdcHit> vtpStream) {
        return vtpStream
                .stream()
                .filter(t -> (t.getTime().compareTo(leading) > 0) && (t.getTime().compareTo(trailing) < 0))
                .collect(Collectors.groupingBy(
                        v -> EUtil.encodeCSC(v.getCrate(), v.getSlot(), v.getChannel()),
                        Collectors.summingInt(AdcHit::getQ)));
    }

    /**
     * Get all slices of the aggregated VTP stream frame.
     *
     *
     * @param frame A single frame of the VTP stream
     * @return list of slices that itself is a map of
     * crate-slot-channels and their integral charge.
     */
    private List<Map<Integer, Integer>> getSlices(List<AdcHit> frame) {
        List<Map<Integer, Integer>> streamSlices = new ArrayList<>();
        for (long i = 0; i < frameLength_ns; i += slice_ns) {
            streamSlices.add(
                    streamSlice(
                            frameStartTime_ns.add(EUtil.toUnsignedBigInteger(i)),
                            frameStartTime_ns.add(EUtil.toUnsignedBigInteger(slice_ns)),
                            frame)
            );
        }
        return streamSlices;
    }

    /**
     * Sliding a fixed time window over the slices of the vtP stream frame.
     *
     * @param slices Slices of the aggregated VTP stream frame
     * @param windowSize size of the window which is a multiple of the sliced window size
     */
    private void _slide(List<Map<Integer, Integer>> slices, int windowSize) {
        SlidingWindowStream
                .slidingStream(slices, windowSize)
                .map(s -> s.collect(Collectors.toList()))
                .forEach(HitFinder::slideWindowSum);
    }

    /**
     * This marches over the stream frame slices and sums the charges for each crate-slot-channel
     * @param slideWindow list of slices, which is map of crate-slot-channel and the charge
     */
    private static void slideWindowSum(List<Map<Integer, Integer>> slideWindow) {
        Map<Integer, Integer> sws = new HashMap<>();
        for (Map<Integer, Integer> m : slideWindow) {
            for (int k : m.keySet()) {
                if (sws.containsKey(k)) {
                    int c = sws.get(k) + m.get(k);
                    sws.put(k, c);
                } else {
                    sws.put(k, m.get(k));
                }
            }
        }

        for (int k : sws.keySet()) {
            if (slideCharges.containsKey(k)) {
                slideCharges.get(k).add(sws.get(k));
            } else {
                List<Integer> l = new ArrayList<>();
                l.add(sws.get(k));
                slideCharges.put(k, l);
            }
        }
    }

    /**
     * Finds hits per crate-slot-channel by finding a pick in a
     * list of charges as a result of window sliding.
     *
     */
    private void findHits() {
        for (Integer csc : slideCharges.keySet()) {
            List<Integer> l = slideCharges.get(csc);
            for (int i = 1; i < l.size() - 1; i++) {
                if ((l.get(i - 1) < l.get(i)) && (l.get(i + 1) < l.get(i))) {
                    BigInteger time = frameStartTime_ns.add(EUtil.toUnsignedBigInteger(i * slice_ns));
                    if (hits.containsKey(csc)) {
                        hits.get(csc).add(new ChargeTime(time, l.get(i)));
                    } else {
                        List<ChargeTime> m = new ArrayList<>();
                        m.add(new ChargeTime(time, l.get(i)));
                        hits.put(csc, m);
                    }
                }
            }
        }
    }
}
