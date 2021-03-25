package org.jlab.epsci.ersap.vtp.util.commons;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jlab.epsci.ersap.vtp.PayloadDecoder;

public class PayloadDecoderPool extends GenericObjectPool<PayloadDecoder> {

    /**
     * Constructor.
     *
     * It uses the default configuration for pool provided by
     * apache-commons-pool2.
     *
     * @param factory
     */
    public PayloadDecoderPool(PooledObjectFactory<PayloadDecoder> factory) {
        super(factory);
    }

    /**
     *
     *
     * @param factory
     * @param config
     */
    public PayloadDecoderPool(PooledObjectFactory<PayloadDecoder> factory,
                 GenericObjectPoolConfig config) {
        super(factory, config);
    }
}