/*
 * Copyright (c) 2021, Jefferson Science Associates, all rights reserved.
 * See LICENSE.txt file.
 *
 * Thomas Jefferson National Accelerator Facility
 * Experimental Physics Software and Computing Infrastructure Group
 *
 * 12000, Jefferson Ave, Newport News, VA 23606
 * Phone : (757)-269-7100
 */


package org.jlab.epsci.stream.sampaBB;
import com.lmax.disruptor.EventFactory;

public class SRingRawEventFactory implements EventFactory<SRingRawEvent> {

    SampaType type;

    SRingRawEventFactory(SampaType type) {
        this.type = type;
    }

    @Override
    public SRingRawEvent newInstance() {
        return new SRingRawEvent(type);
    }
}
