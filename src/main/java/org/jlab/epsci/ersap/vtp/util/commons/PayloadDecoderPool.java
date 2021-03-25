package org.jlab.epsci.ersap.vtp.util.commons;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jlab.epsci.ersap.vtp.engines.format.PayloadDecoderProto;

public class PayloadDecoderPool extends GenericObjectPool<PayloadDecoderProto> {

    /**
     * Constructor.
     *
     * It uses the default configuration for pool provided by
     * apache-commons-pool2.
     *
     * @param factory
     */
    public PayloadDecoderPool(PooledObjectFactory<PayloadDecoderProto> factory) {
        super(factory);
    }

    /**
     *
     *
     * @param factory
     * @param config
     */
    public PayloadDecoderPool(PooledObjectFactory<PayloadDecoderProto> factory,
                 GenericObjectPoolConfig config) {
        super(factory, config);
    }
}