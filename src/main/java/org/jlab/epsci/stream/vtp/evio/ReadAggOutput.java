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

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class ReadAggOutput {

    static int readFile(String finalFilename) throws Exception {

        try {
            EvioReader reader = new EvioReader(finalFilename);
            ByteOrder order = reader.getByteOrder();


            int evCount = reader.getEventCount();
            System.out.println("Read in file " + finalFilename + ", got " + evCount + " events");

            // Loop through all events
            for (int i = 3; i <= evCount; i++) {

                System.out.println("Event " + i + ":");
                EvioEvent ev = reader.getEvent(i);
                ev.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                System.out.println(String.format("%x", ev.getHeader().getTag()));
                // Go one level down ->
                int childCount = ev.getChildCount();
                if (childCount < 2) {
                    throw new Exception("Problem: too few child for event (" + childCount + ")");
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
                        throw new Exception("Problem: too few child for TSB (" + childCount + ")");
                    }

                    // Another level down, each TSB has a Stream Info Bank (SIB) which comes first,
                    // followed by data banks

                    // Skip over SIB by starting at 1
                    for (int k = 1; k < kids; k++) {
                        EvioBank dataBank = (EvioBank) rocTSB.getChildAt(k);
                        // here is where you get data
                        DataType type = dataBank.getHeader().getDataType();
                        System.out.println("    Data type in data bank " + (k - 1) +
                                " is " + type.toString());
                        switch (type) {
                            case INT32:
                                // do some stuff
                                int[] iData = dataBank.getIntData();
                                List<VAdcHit> hits = fADCPayloadDecoder(timestamp, iData);
                                for (VAdcHit h : hits) {
                                    System.out.println(h);
                                }
                                break;
                            default:
                        }
                    }
                }
            }
        } catch (IOException | EvioException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static List<VAdcHit> fADCPayloadDecoder(Long frame_time_ns,
                                                   int[] pData) {
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




