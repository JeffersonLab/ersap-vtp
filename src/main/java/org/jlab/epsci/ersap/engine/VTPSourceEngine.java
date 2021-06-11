package org.jlab.epsci.ersap.engine;

import org.jlab.clara.engine.EngineDataType;
import org.jlab.clara.std.services.AbstractEventReaderService;
import org.jlab.clara.std.services.EventReaderException;
import org.jlab.epsci.ersap.engine.util.StreamingDataTypes;
import org.jlab.epsci.ersap.vtp.VTPTwoStreamAggregatorDecoder;
import org.json.JSONObject;

import java.nio.ByteOrder;
import java.nio.file.Path;

public class VTPSourceEngine extends AbstractEventReaderService<VTPTwoStreamAggregatorDecoder> {
    private static final String VTP_PORT1 = "port1";
    private static final String VTP_PORT2 = "port2";

    @Override
    protected VTPTwoStreamAggregatorDecoder createReader(Path file, JSONObject opts)
            throws EventReaderException {
        int port1 = opts.has(VTP_PORT1) ? opts.getInt(VTP_PORT1) : 6000;
        int port2 = opts.has(VTP_PORT2) ? opts.getInt(VTP_PORT2) : 6001;
        try {
            return new VTPTwoStreamAggregatorDecoder(port1, port2);
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
        return 0;
    }

    @Override
    protected ByteOrder readByteOrder() throws EventReaderException {
        return ByteOrder.LITTLE_ENDIAN;
    }

    @Override
    protected Object readEvent(int eventNumber) throws EventReaderException {
        try {
            reader.go();
            return reader.getDecodedEvent();
        } catch (Exception e) {
            throw new EventReaderException(e);
        }
    }

    @Override
    protected EngineDataType getDataType() {
        return StreamingDataTypes.VTP_G;
    }
}
