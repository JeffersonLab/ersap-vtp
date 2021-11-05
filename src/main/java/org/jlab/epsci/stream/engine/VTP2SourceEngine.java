package org.jlab.epsci.stream.engine;

import org.jlab.epsci.ersap.engine.EngineDataType;
import org.jlab.epsci.ersap.std.services.AbstractEventReaderService;
import org.jlab.epsci.ersap.std.services.EventReaderException;
import org.jlab.epsci.stream.vtp.VTPTwoStreamAggregatorDecoder;
import org.json.JSONObject;

import java.nio.ByteOrder;
import java.nio.file.Path;

public class VTP2SourceEngine extends AbstractEventReaderService<VTPTwoStreamAggregatorDecoder> {
    private static final String VTP_PORT1 = "port1";
    private static final String VTP_PORT2 = "port2";

    @Override
    protected VTPTwoStreamAggregatorDecoder createReader(Path file, JSONObject opts)
            throws EventReaderException {
        int port1 = opts.has(VTP_PORT1) ? opts.getInt(VTP_PORT1) : 6000;
        int port2 = opts.has(VTP_PORT2) ? opts.getInt(VTP_PORT2) : 6001;
        try {
            VTPTwoStreamAggregatorDecoder v = new VTPTwoStreamAggregatorDecoder(port1, port2);
            v.go();
            return v;
        } catch (Exception e) {
            throw new EventReaderException(e);
        }
    }

    @Override
    protected void closeReader() {
      reader.close();
    }

    @Override
    protected int readEventCount() throws EventReaderException {
        return Integer.MAX_VALUE;
    }

    @Override
    protected ByteOrder readByteOrder() throws EventReaderException {
        return ByteOrder.LITTLE_ENDIAN;
    }

    @Override
    protected Object readEvent(int eventNumber) throws EventReaderException {
        try {
            return reader.getDecodedEvent();
        } catch (Exception e) {
            throw new EventReaderException(e);
        }
    }

    @Override
    protected EngineDataType getDataType() {
        return EngineDataType.BYTES;
    }
}
