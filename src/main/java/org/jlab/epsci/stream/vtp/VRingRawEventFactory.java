package org.jlab.epsci.stream.vtp;

import com.lmax.disruptor.EventFactory;

public class VRingRawEventFactory implements EventFactory<VRingRawEvent> {

    @Override
    public VRingRawEvent newInstance() {
        return new VRingRawEvent();
    }
}
