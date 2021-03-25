package org.jlab.epsci.ersap.vtp.engines.format;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PayloadDecoderProto {
    private final AdcDataFormat.AdcHitMap.Builder hitmap;
    private final List<Integer> pData;

    public PayloadDecoderProto(){
        hitmap = AdcDataFormat.AdcHitMap.newBuilder();
        pData = new ArrayList<>();
    }

    public void decode(Long frame_time_ns, ByteBuffer buf, int s1, int s2) {
        pData.clear();
        hitmap.clear();

        buf.rewind();
        while (buf.hasRemaining()) {
            pData.add(buf.getInt());
        }
        buf.clear();
        corePayloadDecoder(frame_time_ns, pData, s1);
        corePayloadDecoder(frame_time_ns, pData, s2);
        hitmap.build();
    }

    public byte[] getEvt() {
        byte[] b = hitmap.build().toByteArray();
        return Arrays.copyOf(b,b.length);
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
                                hitmap.addCrate(crate);
                                hitmap.addSlot(slot);
                                hitmap.addChannel(channel);
                                hitmap.addCharge(q);
                                hitmap.addTime(ht);
                            }
                        }
                    }
                }
            }
        }
    }

    public void dump() {
        System.out.println("\n========================================= ");
        hitmap.toString();
    }

}
