package org.jlab.epsci.stream.vtp;

import java.nio.ByteBuffer;

import static org.jlab.epsci.stream.util.EUtil.cloneByteBuffer;

public class VAdcHitMapBa {
    private ByteBuffer evt;

    public VAdcHitMapBa(int size) {
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
