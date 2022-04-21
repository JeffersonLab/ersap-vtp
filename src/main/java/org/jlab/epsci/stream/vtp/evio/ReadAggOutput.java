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

public class ReadAggOutput {

    static int readFile(String finalFilename) throws Exception {

        try {
            EvioReader reader = new EvioReader(new File(finalFilename), false, true, false);
            ByteOrder order = reader.getByteOrder();

            int evCount = reader.getEventCount();
            System.out.println("Read in file " + finalFilename + ", got " + evCount + " events");

            // Loop through all events (skip first 2 which are prestart and go)
            for (int i=1; i <= evCount; i++) {

                System.out.println("Event " + i + ":");
                EvioEvent ev = reader.parseEvent(i);
                int evTag = ev.getHeader().getTag();
                if (evTag == 0xffd1) {
                    System.out.println("Skip over PRESTART event");
                    continue;
                }
                else if (evTag == 0xffd2) {
                    System.out.println("Skip over GO event");
                    continue;
                }
                else if (evTag == 0xffd4) {
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
                long timestamp = ((((long)intData[1]) & 0x00000000ffffffffL) +
                        (((long)intData[2]) << 32));
                System.out.println("  Frame = " + frame + ", TS = " + timestamp);

                // Loop through all ROC Time Slice Banks (TSB) which come after TIB
                for (int j=1; j < childCount; j++) {
                    // ROC Time SLice Bank
                    EvioBank rocTSB = (EvioBank)ev.getChildAt(j);
                    int kids = rocTSB.getChildCount();
                    if (kids < 2) {
                        throw new Exception("Problem: too few child for TSB (" + childCount + ")");
                    }

                    // Another level down, each TSB has a Stream Info Bank (SIB) which comes first,
                    // followed by data banks

                    // Skip over SIB by starting at 1
                    for (int k=1; k < kids; k++) {
                        EvioBank dataBank = (EvioBank) rocTSB.getChildAt(k);
                        // Ignore the data type (currently the improper value of 0xf).
                        // Just get the data as bytes
                        byte[] byteData = dataBank.getRawBytes();
                        List<VAdcHit> hits = fADCPayloadDecoder(timestamp, byteData);
                        for (VAdcHit h : hits) {
                            System.out.println(h);
                        }

                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (EvioException e) {
            e.printStackTrace();
        }

        return 0;
    }



    public static List<VAdcHit> fADCPayloadDecoder(Long frame_time_ns, byte[] ba) {
        IntBuffer intBuf =
                ByteBuffer.wrap(ba)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .asIntBuffer();
        int[] pData = new int[intBuf.remaining()];
        intBuf.get(pData);

        List<VAdcHit> ev_list = new ArrayList<>();
        if (pData.length != 0) {
            if ((pData[0] & 0x8FFF8000) == 0x80000000) {
                for (int j = 1; j < 9; j++) {
                    int vl = pData[j];
                    int slot_ind = (vl >> 0) & 0xFFFF;
                    int slot_len = (vl >> 16) & 0xFFFF;
                    if (slot_len > 0) {
                        int type = 0x0;
                        int crate = -1;
                        int slot = -1;
                        for (int jj = 0; jj < slot_len; jj++) {
                            int val = pData[slot_ind + jj];
                            if ((val & 0x80000000) == 0x80000000) {
                                type = (val >> 15) & 0xFFFF;
                                crate = (val >> 8) & 0x007F;
                                slot = (val >> 0) & 0x001F;
                            } else if (type == 0x0001) { // FADC hit type
                                int q = (val >> 0) & 0x1FFF;
                                int channel = (val >> 13) & 0x000F;
                                long v = ((val >> 17) & 0x3FFF) * 4;
                                long ht = frame_time_ns + v;
                                ev_list.add(new VAdcHit(crate, slot, channel, q, ht));
                            }
                        }
                    }
                }
            }
        }
        return ev_list;
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




