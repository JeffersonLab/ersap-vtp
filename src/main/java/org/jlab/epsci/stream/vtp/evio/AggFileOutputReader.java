package org.jlab.epsci.stream.vtp.evio;

/**
 * Copyright (c) 2021, Jefferson Science Associates, all rights reserved.
 * See LICENSE.txt file.
 * Thomas Jefferson National Accelerator Facility
 * Experimental Physics Software and Computing Infrastructure Group
 * 12000, Jefferson Ave, Newport News, VA 23606
 * Phone : (757)-269-7100
 *
 * @author gurjyan on 4/20/22
 * @project ersap-vtp
 */


import org.jlab.coda.jevio.*;
import org.jlab.epsci.stream.vtp.VAdcHit;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class AggFileOutputReader {

    private String fName;
    private EvioReader reader;
    private int evCount;
    private int evtIndex = 0; //for evio starts from 1
    private ByteOrder order;

    public AggFileOutputReader(String fName) {
        this.fName = fName;
        try {
            reader = new EvioReader(new File(fName), false, true, false);
            order = reader.getByteOrder();
            evCount = reader.getEventCount();
        } catch (IOException | EvioException e) {
            e.printStackTrace();
        }
        System.out.println("Read in file " + fName + ", got " + evCount + " events");

    }

    public AggFileOutputReader(File file) {
        try {
            reader = new EvioReader(file, false, true, false);
            order = reader.getByteOrder();
            evCount = reader.getEventCount();
        } catch (IOException | EvioException e) {
            e.printStackTrace();
        }
        System.out.println("Read in file " + file.getName() + ", got " + evCount + " events");

    }

    public EvioEvent nextEvent() {
        evtIndex++;
//        System.out.println("DDD =========== > Reading event = "+evtIndex);
        if (evtIndex <= evCount) {
            try {
                return reader.parseEvent(evtIndex);
            } catch (IOException | EvioException e) {
                e.printStackTrace();
                return null;
            }
        } else return null;
    }

    public int getEventCount() {
        return evCount;
    }

    public ByteOrder getByteOrder() {
        return order;
    }

    public void close(){
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static int readFile(String finalFilename) throws Exception {

        try {
            EvioReader reader = new EvioReader(new File(finalFilename), false, true, false);
            ByteOrder order = reader.getByteOrder();

            int evCount = reader.getEventCount();
            System.out.println("Read in file " + finalFilename + ", got " + evCount + " events");

            // Loop through all events (skip first 2 which are prestart and go)
            for (int i = 1; i <= evCount; i++) {

                System.out.println("Event " + i + ":");
                EvioEvent ev = reader.parseEvent(i);
                int evTag = ev.getHeader().getTag();
                if (evTag == 0xffd1) {
                    System.out.println("Skip over PRESTART event");
                    continue;
                } else if (evTag == 0xffd2) {
                    System.out.println("Skip over GO event");
                    continue;
                } else if (evTag == 0xffd4) {
                    System.out.println("Hit END event, quitting");
                    break;
                }

                if (evTag == 0xff60) {
                    System.out.println("Found built streaming event");
                }

                // Go one level down ->
                int childCount = ev.getChildCount();
                if (childCount < 2) {
                    throw new Exception("Problem: too few child for event (" + childCount + ")");
                }
//                System.out.println("Event has " + childCount + " child structures");

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
                        throw new Exception("Problem: too few child for TSB (" + childCount + ")");
                    }

                    // Another level down, each TSB has a Stream Info Bank (SIB) which comes first,
                    // followed by data banks

                    // Skip over SIB by starting at 1
                    for (int k = 1; k < kids; k++) {
                        EvioBank dataBank = (EvioBank) rocTSB.getChildAt(k);
                        // Ignore the data type (currently the improper value of 0xf).
                        // Just get the data as bytes
                        int payloadId = dataBank.getHeader().getTag();
                        System.out.println("payload ID = " + payloadId);
                        byte[] byteData = dataBank.getRawBytes();
                        List<VAdcHit> hits = fADCPayloadDecoder(timestamp, payloadId, byteData);
                        for (VAdcHit h : hits) {
                            System.out.println( "DDD "+ h);
                        }
                    }
                }
            }
        } catch (IOException | EvioException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static List<VAdcHit> fADCPayloadDecoder(Long frame_time_ns, int payloadId, byte[] ba) {
        List<VAdcHit> hits = new ArrayList<>();
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
            hits.add(new VAdcHit(1, payloadId, channel, q, ht));
        }
        return hits;
    }

    public static void main(String args[]) {
        try {
            readFile(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----------------------------------------\n");
    }
}




