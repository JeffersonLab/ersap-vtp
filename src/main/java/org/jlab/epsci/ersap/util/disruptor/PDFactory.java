package org.jlab.epsci.ersap.util.disruptor;

import com.lmax.disruptor.EventFactory;
import org.jlab.epsci.ersap.vtp.VPayloadDecoder;

public class PDFactory implements EventFactory<VPayloadDecoder> {
    @Override
    public VPayloadDecoder newInstance() {
        return new VPayloadDecoder();
    }

}
