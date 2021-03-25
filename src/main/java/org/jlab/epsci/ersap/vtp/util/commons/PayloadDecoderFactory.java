package org.jlab.epsci.ersap.vtp.util.commons;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.jlab.epsci.ersap.vtp.PayloadDecoder;

public class PayloadDecoderFactory extends BasePooledObjectFactory<PayloadDecoder> {

    @Override
    public PayloadDecoder create() throws Exception {
        return new PayloadDecoder();
    }

    @Override
    public PooledObject<PayloadDecoder> wrap(PayloadDecoder payloadDecoder) {
        return new DefaultPooledObject<PayloadDecoder>(payloadDecoder);
    }
}
