package org.jlab.epsci.stream.engine.util;

import j4np.data.evio.EvioEvent;
import org.jlab.epsci.ersap.base.error.ErsapException;
import org.jlab.epsci.ersap.engine.EngineDataType;
import org.jlab.epsci.ersap.engine.ErsapSerializer;

import java.nio.ByteBuffer;

public final class EvioDataType {

    private EvioDataType() { }

    private static class EvioSerializer implements ErsapSerializer{

        @Override
        public ByteBuffer write(Object data) throws ErsapException {
            EvioEvent event = (EvioEvent)data;
            return event.getBuffer();
        }

        @Override
        public Object read(ByteBuffer buffer) throws ErsapException {
            return new EvioEvent(); //todo how to create EvioReader object from a byteBuffer
        }
    }

    public static final EngineDataType EVIO =
            new EngineDataType("binary/data-evio", EngineDataType.BYTES.serializer());
}