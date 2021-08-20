package org.jlab.epsci.stream.engine.util;

import org.jlab.epsci.ersap.base.error.ErsapException;
import org.jlab.epsci.ersap.engine.EngineDataType;
import org.jlab.epsci.ersap.engine.ErsapSerializer;

import java.nio.ByteBuffer;

public final class EvioDataType {

    private EvioDataType() { }

    private static class EvioSerializer implements ErsapSerializer{

        @Override
        public ByteBuffer write(Object data) throws ErsapException {
            return null;
        }

        @Override
        public Object read(ByteBuffer buffer) throws ErsapException {
            return null;
        }
    }


    public static final EngineDataType EVIO =
            new EngineDataType("binary/data-hipo", new EvioSerializer());
}