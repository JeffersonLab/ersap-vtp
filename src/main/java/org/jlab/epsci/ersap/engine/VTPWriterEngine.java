package org.jlab.epsci.ersap.engine;

import org.jlab.clara.engine.EngineDataType;
import org.jlab.clara.std.services.AbstractEventWriterService;
import org.jlab.clara.std.services.EventWriterException;
import org.json.JSONObject;

import java.nio.file.Path;

public class VTPWriterEngine extends AbstractEventWriterService<Object> {
    @Override
    protected Object createWriter(Path file, JSONObject opts) throws EventWriterException {
        return null;
    }

    @Override
    protected void closeWriter() {

    }

    @Override
    protected void writeEvent(Object event) throws EventWriterException {

    }

    @Override
    protected EngineDataType getDataType() {
        return null;
    }
}
