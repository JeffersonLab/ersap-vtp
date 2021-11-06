package org.jlab.epsci.stream.vtp;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.jlab.epsci.stream.util.EUtil.cloneByteBuffer;

public class VAdcHitMap {
    private ByteBuffer evt;
    private List<VAdcHit> ev_list = new ArrayList<>();
    private int evtSize;

    public VAdcHitMap(int size) {
        evt = ByteBuffer.allocate(size);
    }

    public void reset() {
        evt.clear();
        evtSize = 0;
        ev_list.clear();
    }


    public void add(long time, int crate, int slot, int channel, int charge) {
        evt.putLong(time);
        evt.putInt(crate);
        evt.putInt(slot);
        evt.putInt(channel);
        evt.putInt(charge);
        evtSize++;
        ev_list.add(new VAdcHit(crate, slot, channel, charge, time));
    }

    public ByteBuffer getEvt(){
        return cloneByteBuffer(evt);
    }

    public List<VAdcHit> getEvList() {
        return ev_list;
    }


    public int getEvtSize(){
        return evtSize;
    }
}
