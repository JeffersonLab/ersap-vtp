package org.jlab.epsci.ersap.vtp;

import com.lmax.disruptor.EventFactory;

public class RingAdcHitEventFactory implements EventFactory<RingAdcHitEvent> {
    @Override
    public RingAdcHitEvent newInstance() {
        return new RingAdcHitEvent();
    }

}
