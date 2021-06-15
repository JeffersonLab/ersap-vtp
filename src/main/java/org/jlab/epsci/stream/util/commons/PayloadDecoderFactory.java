package org.jlab.epsci.stream.util.commons;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.jlab.epsci.stream.vtp.VPayloadDecoder;

public class PayloadDecoderFactory extends BasePooledObjectFactory<VPayloadDecoder> {

    @Override
    public VPayloadDecoder create() throws Exception {
        return new VPayloadDecoder();
    }

    @Override
    public PooledObject<VPayloadDecoder> wrap(VPayloadDecoder payloadDecoder) {
        return new DefaultPooledObject<VPayloadDecoder>(payloadDecoder);
    }
}
