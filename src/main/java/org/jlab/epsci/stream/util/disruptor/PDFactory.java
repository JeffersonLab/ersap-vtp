package org.jlab.epsci.stream.util.disruptor;

import com.lmax.disruptor.EventFactory;
import org.jlab.epsci.stream.vtp.VPayloadDecoder;

public class PDFactory implements EventFactory<VPayloadDecoder> {
    @Override
    public VPayloadDecoder newInstance() {
        return new VPayloadDecoder();
    }

}
