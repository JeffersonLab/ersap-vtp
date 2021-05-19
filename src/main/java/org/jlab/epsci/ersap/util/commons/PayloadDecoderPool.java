package org.jlab.epsci.ersap.util.commons;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jlab.epsci.ersap.vtp.VPayloadDecoder;

public class PayloadDecoderPool extends GenericObjectPool<VPayloadDecoder> {

    /**
     * Constructor.
     *
     * It uses the default configuration for pool provided by
     * apache-commons-pool2.
     *
     * @param factory
     */
    public PayloadDecoderPool(PooledObjectFactory<VPayloadDecoder> factory) {
        super(factory);
    }

    /**
     *
     *
     * @param factory
     * @param config
     */
    public PayloadDecoderPool(PooledObjectFactory<VPayloadDecoder> factory,
                 GenericObjectPoolConfig config) {
        super(factory, config);
    }
}