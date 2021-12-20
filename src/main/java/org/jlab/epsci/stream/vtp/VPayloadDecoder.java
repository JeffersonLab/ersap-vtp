package org.jlab.epsci.stream.vtp;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class VPayloadDecoder {
    private final VAdcHitMap evt;
    private final List<Integer> pData;

    private int prescale;
    private static int PVALUE = 50;

    public VPayloadDecoder(){
        evt = new VAdcHitMap(2000000);
        pData = new ArrayList<>();
        prescale = PVALUE;
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
    public void decode(Long frame_time_ns, ByteBuffer buf) {
        // analyze every prescale event
//        if ((prescale -= 1) > 0) return;
//        prescale = PVALUE;

        pData.clear();
        evt.reset();

        buf.rewind();
        while (buf.hasRemaining()) {
            pData.add(buf.getInt());
        }
        buf.clear();
        corePayloadDecoder(frame_time_ns, pData, 0);
//        dump(evt.getEvList()); // dump entire frame
        eventIdentificationAndWriting(4L,3); // print coincidences within 4 ns window

    }


    public ByteBuffer getEvt() {
        return evt.getEvt();
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

        public void dump( List<VAdcHit> hit_map) {
        System.out.println("\n========================================= ");
        if (hit_map.size() < 0) {
            System.out.println("\nWarning: hit-map is inconsistent");
        } else {
            for (VAdcHit hit:hit_map) {
                System.out.println(hit);
            }
        }
    }

    public void eventIdentificationAndWriting(long width, int level) {
        List<VAdcHit> tmp_res = new ArrayList<>();
        if(evt.getEvtSize() > 0) {
            long leadingEdge = evt.getEvList().get(0).getTime();
            for (VAdcHit hit:evt.getEvList()) {
                if(hit.getTime() > leadingEdge + width) {
                    leadingEdge = hit.getTime();

                        // write event to hipo file
                    if(tmp_res.size() >= level) {
                        VTPOneStreamReceiverDecoder.hipoFile.evtWrite(tmp_res);
                        VTPOneStreamReceiverDecoder.ebEvents++;
//                        dump(tmp_res);
                    }
                    tmp_res.clear();
                } else {
                    tmp_res.add(hit);
                }
            }
        }
    }
}
