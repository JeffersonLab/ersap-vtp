package org.jlab.epsci.ersap.vtp.util.disruptor;

import com.lmax.disruptor.EventFactory;
import org.jlab.epsci.ersap.vtp.PayloadDecoder;

public class PDFactory implements EventFactory<PayloadDecoder> {
    @Override
    public PayloadDecoder newInstance() {
        return new PayloadDecoder();
    }

}
