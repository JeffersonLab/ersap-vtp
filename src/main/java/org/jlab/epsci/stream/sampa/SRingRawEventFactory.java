package org.jlab.epsci.stream.sampa;
import com.lmax.disruptor.EventFactory;

public class SRingRawEventFactory implements EventFactory<SRingRawEvent> {

    @Override
    public SRingRawEvent newInstance() {
        return new SRingRawEvent();
    }
}
