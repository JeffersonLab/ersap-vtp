package org.jlab.epsci.stream.util.commons;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.nio.ByteBuffer;

public class ByteBufferPool extends GenericObjectPool<ByteBuffer> {

    public ByteBufferPool(PooledObjectFactory<ByteBuffer> factory) {
        super(factory);
    }

    public ByteBufferPool(PooledObjectFactory<ByteBuffer> factory, GenericObjectPoolConfig<ByteBuffer> config) {
        super(factory, config);
    }

    public ByteBufferPool(PooledObjectFactory<ByteBuffer> factory, GenericObjectPoolConfig<ByteBuffer> config,
                          AbandonedConfig abandonedConfig) {
        super(factory, config, abandonedConfig);
    }
}
