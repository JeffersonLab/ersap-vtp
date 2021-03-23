package org.jlab.epsci.ersap.vtp;

import com.lmax.disruptor.EventFactory;

public class PayloadDecoderFactory implements EventFactory<PayloadDecoder> {
    @Override
    public PayloadDecoder newInstance() {
        return new PayloadDecoder();
    }

}
