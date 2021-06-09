package org.jlab.epsci.ersap.engine;

import org.jlab.clara.engine.EngineDataType;
import org.jlab.clara.std.services.AbstractEventReaderService;
import org.jlab.clara.std.services.EventReaderException;
import org.json.JSONObject;

import java.nio.ByteOrder;
import java.nio.file.Path;

public class SAMPASourceEngine extends AbstractEventReaderService<SAMPASourceEngine> {

    @Override
    protected SAMPASourceEngine createReader(Path file, JSONObject opts) throws EventReaderException {
        return null;
    }

    @Override
    protected void closeReader() {

    }

    @Override
    protected int readEventCount() throws EventReaderException {
        return 0;
    }

    @Override
    protected ByteOrder readByteOrder() throws EventReaderException {
        return null;
    }

    @Override
    protected Object readEvent(int eventNumber) throws EventReaderException {
        return null;
    }

    @Override
    protected EngineDataType getDataType() {
        return null;
    }
}
