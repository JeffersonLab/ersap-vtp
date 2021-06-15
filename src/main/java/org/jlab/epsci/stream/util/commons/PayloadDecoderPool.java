package org.jlab.epsci.stream.util.commons;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jlab.epsci.stream.vtp.VPayloadDecoder;

public class PayloadDecoderPool extends GenericObjectPool<VPayloadDecoder> {

    public PayloadDecoderPool(PooledObjectFactory<VPayloadDecoder> factory) {
        super(factory);
    }

    public PayloadDecoderPool(PooledObjectFactory<VPayloadDecoder> factory,
                 GenericObjectPoolConfig config) {
        super(factory, config);
    }
}