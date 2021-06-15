package org.jlab.epsci.stream.engine.util;

import org.jlab.epsci.ersap.engine.EngineDataType;

public final class StreamingDataTypes {

    public static final EngineDataType VTP_G =
            new EngineDataType("binary/data-vtp-g", EngineDataType.BYTES.serializer());
}
