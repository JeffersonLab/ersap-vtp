package org.jlab.epsci.ersap.vtp;

import com.lmax.disruptor.EventFactory;

public class RingRawEventFactory implements EventFactory<RingRawEvent> {

    @Override
    public RingRawEvent newInstance() {
        return new RingRawEvent();
    }
}
