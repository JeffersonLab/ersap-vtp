package org.jlab.epsci.ersap.vtp.util.commons;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.jlab.epsci.ersap.vtp.engines.format.PayloadDecoderProto;

public class PayloadDecoderFactory extends BasePooledObjectFactory<PayloadDecoderProto> {

    @Override
    public PayloadDecoderProto create() throws Exception {
        return new PayloadDecoderProto();
    }

    @Override
    public PooledObject<PayloadDecoderProto> wrap(PayloadDecoderProto payloadDecoder) {
        return new DefaultPooledObject<PayloadDecoderProto>(payloadDecoder);
    }
}
