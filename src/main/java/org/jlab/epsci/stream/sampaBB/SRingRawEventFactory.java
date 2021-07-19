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

    private final SampaType type;
    private final boolean forAggregation;

    /**
     * Constructor of factory which produces event for a disruptor's ring buffer.
     * This events produced by this factory will, by default, only hald data from
     * a single stream.
     * @param type type of data coming from SAMPA board
     */
    SRingRawEventFactory(SampaType type) {this(type, false);}

    /**
     * Constructor of factory which produces event for a disruptor's ring buffer.
     * @param type type of data coming from SAMPA board
     * @param forAggregation if true, this is used to hold aggregated data -
     *                       all 160 channels of a SAMPA board. Or, in other words,
     *                       it needs hold 2x the data coming from a single stream.
     */
    SRingRawEventFactory(SampaType type, boolean forAggregation) {
        this.type = type;
        this.forAggregation = forAggregation;
    }

    @Override
    public SRingRawEvent newInstance() {
        return new SRingRawEvent(type, forAggregation);
    }
}
