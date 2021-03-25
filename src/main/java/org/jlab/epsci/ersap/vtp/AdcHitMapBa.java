package org.jlab.epsci.ersap.vtp;

import java.nio.ByteBuffer;

import static org.jlab.epsci.ersap.vtp.util.EUtil.cloneByteBuffer;

public class AdcHitMapBa {
    private ByteBuffer evt;

    public AdcHitMapBa(int size) {
        evt = ByteBuffer.allocate(size);
    }

    public void reset() {
        evt.clear();
    }


    public void add(long time, int crate, int slot, int channel, int charge) {
        evt.putLong(time);
        evt.putInt(crate);
        evt.putInt(slot);
        evt.putInt(channel);
        evt.putInt(charge);
    }

    public ByteBuffer getEvt(){
        return cloneByteBuffer(evt);
    }

}
