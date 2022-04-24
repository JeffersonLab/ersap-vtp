package org.jlab.epsci.stream.engine.evio;

import org.jlab.coda.jevio.EvioBank;
import org.jlab.coda.jevio.EvioEvent;
import org.jlab.epsci.ersap.base.ErsapUtil;
import org.jlab.epsci.ersap.engine.Engine;
import org.jlab.epsci.ersap.engine.EngineData;
import org.jlab.epsci.ersap.engine.EngineDataType;
import org.jlab.epsci.stream.engine.util.EvioDataType;
import org.jlab.epsci.stream.vtp.VAdcHit;
import org.json.JSONObject;
import twig.data.H1F;
import twig.graphics.TGDataCanvas;

import javax.swing.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2021, Jefferson Science Associates, all rights reserved.
 * See LICENSE.txt file.
 * Thomas Jefferson National Accelerator Facility
 * Experimental Physics Software and Computing Infrastructure Group
 * 12000, Jefferson Ave, Newport News, VA 23606
 * Phone : (757)-269-7100
 *
 * @author gurjyan on 4/23/22
 * @project ersap-vtp
 */
public class EvioFadcDecoderEngine implements Engine {

    private static String S_WINDOW = "sliding_widow_size";
    private int slidingWindowSize;
    private static String S_STEP = "sliding_step";
    private int stepSize;

    private static int[] slotMap = {0, 10, 13, 9, 14, 8, 15, 7, 16, 6, 17, 5, 18, 4, 19, 3, 20};

    @Override
    public EngineData configure(EngineData input) {
        if (input.getMimeType().equalsIgnoreCase(EngineDataType.JSON.mimeType())) {
            String source = (String) input.getData();
            JSONObject data = new JSONObject(source);
            slidingWindowSize = data.has(S_WINDOW) ? data.getInt(S_WINDOW) : 16;
            stepSize = data.has(S_STEP) ? data.getInt(S_STEP) : 1;
        }

//        // Histogram preparation
//        JFrame frame = new JFrame("Canvas");
//        TGDataCanvas c = new TGDataCanvas();
//
//        frame.add(c);
//        frame.setSize(600, 600);
//        frame.setVisible(true);
//
//        c.initTimer(600);
//        H1F h1 = new H1F(slot + "_c1", 100, 0.0, 8000.0);
//        H1F h2 = new H1F(slot + "_c2", 100, 0.0, 8000.0);
//        H1F h3 = new H1F(slot + "_c3", 100, 0.0, 8000.0);
//        H1F h4 = new H1F(slot + "_c4", 100, 0.0, 8000.0);
//        H1F h5 = new H1F(slot + "_c5", 100, 0.0, 8000.0);
//        H1F h6 = new H1F(slot + "_c6", 100, 0.0, 8000.0);
//        H1F h7 = new H1F(slot + "_c7", 100, 0.0, 8000.0);
//        H1F h8 = new H1F(slot + "_c8", 100, 0.0, 8000.0);
//        H1F h9 = new H1F(slot + "_c9", 100, 0.0, 8000.0);
//        H1F h10 = new H1F(slot + "_c10", 100, 0.0, 8000.0);
//        H1F h11 = new H1F(slot + "_c11", 100, 0.0, 8000.0);
//        H1F h12 = new H1F(slot + "_c12", 100, 0.0, 8000.0);
//        H1F h13 = new H1F(slot + "_c13", 100, 0.0, 8000.0);
//        H1F h14 = new H1F(slot + "_c14", 100, 0.0, 8000.0);
//        H1F h15 = new H1F(slot + "_c15", 100, 0.0, 8000.0);
//        H1F h16 = new H1F(slot + "_c16", 100, 0.0, 8000.0);
//
//        c.region().draw(h1);

        return null;
    }

    @Override
    public EngineData execute(EngineData input) {
        String dataType = input.getMimeType();
        if (dataType.equals(EvioDataType.EVIO.mimeType())) {
            EvioEvent ev = (EvioEvent) input.getData();
            int evTag = ev.getHeader().getTag();
            if (evTag == 0xffd1) {
                System.out.println("Skip over PRESTART event");
            } else if (evTag == 0xffd2) {
                System.out.println("Skip over GO event");
            } else if (evTag == 0xffd4) {
                System.out.println("Hit END event, quitting");
                return input;
            }

            if (evTag == 0xff60) {
                System.out.println("Found built streaming event");
            }

            // Go one level down ->
            int childCount = ev.getChildCount();
            if (childCount < 2) {
                System.out.println("Problem: too few child for event (" + childCount + ")");
            }
            // First bank is Time Info Bank (TIB) with frame and timestamp
            EvioBank b = (EvioBank) ev.getChildAt(0);
            int[] intData = b.getIntData();
            int frame = intData[0];
            long timestamp = ((((long) intData[1]) & 0x00000000ffffffffL) +
                    (((long) intData[2]) << 32));
            System.out.println("  Frame = " + frame + ", TS = " + timestamp);

            // Loop through all ROC Time Slice Banks (TSB) which come after TIB
            for (int j = 1; j < childCount; j++) {
                // ROC Time SLice Bank
                EvioBank rocTSB = (EvioBank) ev.getChildAt(j);
                int kids = rocTSB.getChildCount();
                if (kids < 2) {
                    System.out.println("Problem: too few child for TSB (" + childCount + ")");
                }

                // Another level down, each TSB has a Stream Info Bank (SIB) which comes first,
                // followed by data banks

                // actual container of all fADC hits in the VTP frame
                Map<Long, List<VAdcHit>> data = new HashMap<>();
                // Skip over SIB by starting at 1
                for (int k = 1; k < kids; k++) {
                    EvioBank dataBank = (EvioBank) rocTSB.getChildAt(k);
                    // Ignore the data type (currently the improper value of 0xf).
                    // Just get the data as bytes
                    int payloadId = dataBank.getHeader().getTag();
                    int slt = getSlot(payloadId);
                    //                    System.out.println("payload ID = " + payloadId);
                    byte[] byteData = dataBank.getRawBytes();
                    // define the fits for a slot in the VTP frame
                    fADCPayloadDecoder(data, timestamp, slt, byteData);
                }

                eventIdentification(data);

            }

        }
        return input;
    }

    private void fADCPayloadDecoder(Map<Long, List<VAdcHit>> data,
                                    Long frame_time_ns,
                                    int payloadId,
                                    byte[] ba) {
        IntBuffer intBuf =
                ByteBuffer.wrap(ba)
                        .order(ByteOrder.BIG_ENDIAN)
                        .asIntBuffer();
        int[] pData = new int[intBuf.remaining()];
        intBuf.get(pData);
        for (int i : pData) {
            int q = (i >> 0) & 0x1FFF;
            int channel = (i >> 13) & 0x000F;
            long v = ((i >> 17) & 0x3FFF) * 4;
            long ht = frame_time_ns + v;
            if (data.containsKey(ht)) {
                data.get(ht).add(new VAdcHit(1, payloadId, channel, q, ht));
            } else {
                List<VAdcHit> hits = new ArrayList<>();
                hits.add(new VAdcHit(1, payloadId, channel, q, ht));
                data.put(ht, hits);
            }
        }
    }

    private Map<String, List<Integer>> eventIdentification(Map<Long, List<VAdcHit>> hits) {
        Map<String, List<Integer>> evIdentified = new HashMap<>();
        long sTime, eTime;
        int step = 0;
        Set<Long> timeStamps = hits.keySet();
        long startFrameTime = Collections.min(timeStamps);
        long endFrameTime = Collections.max(timeStamps);
        do {
            sTime = startFrameTime + ((long) step * stepSize);
            eTime = sTime + slidingWindowSize;
            step++;
            final long s = sTime;
            final long e = eTime;

            Map<Long, List<VAdcHit>> subMap = hits.entrySet().stream()
                    .filter(x -> (x.getKey() >= s) && (x.getKey() <= e))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // event identification
            Map<String, List<Integer>> slidingWindowHits = new HashMap<>();
            for (List<VAdcHit> l : subMap.values()) {
                for (VAdcHit h : l) {
                    String n = h.getCrate()
                            + "_" + h.getSlot()
                            + " " + h.getCrate();

                    if (slidingWindowHits.containsKey(n)) {
                        slidingWindowHits.get(n).add(h.getCharge());
                    } else {
                        List<Integer> ll = new ArrayList<>();
                        ll.add(h.getCharge());
                        slidingWindowHits.put(n, ll);
                    }
                }
            }
            boolean foundEvent = true;
            Map<String, Integer> evtCandidate = new HashMap<>();
            for (var entry : slidingWindowHits.entrySet()) {
                if (entry.getValue().size() > 1) {
                    foundEvent = false;
                    evtCandidate.clear();
                    break;
                } else {
                    evtCandidate.put(entry.getKey(), entry.getValue().get(0));
                }
            }
            if (foundEvent) {
                // add to the identified events container
                for (var entry : evtCandidate.entrySet()) {
                    if (evIdentified.containsKey(entry.getKey())) {
                        evIdentified.get(entry.getKey()).add(entry.getValue());
                    } else {
                        List<Integer> lo = new ArrayList<>();
                        lo.add(entry.getValue());
                        evIdentified.put(entry.getKey(), lo);
                    }
                }
            }
        } while (eTime >= endFrameTime);
        return evIdentified;
    }

    private int getSlot(int payloadId) {
        return slotMap[payloadId];
    }

    @Override
    public EngineData executeGroup(Set<EngineData> inputs) {
        return null;
    }

    @Override
    public Set<EngineDataType> getInputDataTypes() {
        return ErsapUtil.buildDataTypes(EvioDataType.EVIO,
                EngineDataType.JSON);
    }

    @Override
    public Set<EngineDataType> getOutputDataTypes() {
        return ErsapUtil.buildDataTypes(EvioDataType.EVIO);
    }

    @Override
    public Set<String> getStates() {
        return null;
    }

    @Override
    public String getDescription() {
        return "fADC data decoder. EVIO data format";
    }

    @Override
    public String getVersion() {
        return "v1.1";
    }

    @Override
    public String getAuthor() {
        return "gurjyan";
    }

    @Override
    public void reset() {

    }

    @Override
    public void destroy() {

    }
}
