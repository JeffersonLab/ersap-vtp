package org.jlab.epsci.ersap.vtp;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class PayloadDecoder {
    private final AdcHitMapEvent evt;
    private final List<Integer> pData;

    public PayloadDecoder(){
        evt = new AdcHitMapEvent();
        pData = new ArrayList<>();
    }

    public void decode(Long frame_time_ns, ByteBuffer buf, int s1, int s2) {
        pData.clear();
        evt.reset();

        buf.rewind();
        while (buf.hasRemaining()) {
            pData.add(buf.getInt());
        }
        buf.clear();
        corePayloadDecoder(frame_time_ns, pData, s1);
        corePayloadDecoder(frame_time_ns, pData, s2);
    }

    private void corePayloadDecoder(Long frame_time_ns,
                                    List<Integer> pData, int pIndex) {
        if (!pData.isEmpty()) {
            if ((pData.get(pIndex) & 0x8FFF8000) == 0x80000000) {
                for (int j = pIndex + 1; j < pIndex + 9; j++) {
                    int vl = pData.get(j);
                    int slot_ind = (vl >> 0) & 0xFFFF;
                    int slot_len = (vl >> 16) & 0xFFFF;
                    if (slot_len > 0) {
                        int type = 0x0;
                        int crate = -1;
                        int slot = -1;
                        for (int jj = 0; jj < slot_len; jj++) {
                            int val = pData.get(slot_ind + pIndex + jj);

                            if ((val & 0x80000000) == 0x80000000) {
                                type = (val >> 15) & 0xFFFF;
                                crate = (val >> 8) & 0x007F;
                                slot = (val >> 0) & 0x001F;
                            } else if (type == 0x0001) { // FADC hit type
                                int q = (val >> 0) & 0x1FFF;
                                int channel = (val >> 13) & 0x000F;
                                long v = ((val >> 17) & 0x3FFF) * 4;
                                long ht = frame_time_ns + v;
                                evt.add(ht, crate, slot, channel, q);
                            }
                        }
                    }
                }
            }
        }
    }

    public void dump() {
        System.out.println("\n========================================= ");
        if (evt.evtSize() < 0) {
            System.out.println("\nWarning: hit-map is inconsistent");
        } else {
            for (int i = 0; i < evt.evtSize(); i++) {
                System.out.println("AdcHit{" +
                        "crate=" + evt.getCrate(i) +
                        ", slot=" + evt.getSlot(i) +
                        ", channel=" + evt.getChannel(i) +
                        ", q=" + evt.getCharge(i) +
                        ", time=" + evt.getTime(i) +
                        '}');

            }
        }
    }

}
