package org.jlab.epsci.ersap.engine;

import org.jlab.clara.engine.EngineDataType;
import org.jlab.clara.std.services.AbstractEventWriterService;
import org.jlab.clara.std.services.EventWriterException;
import org.jlab.epsci.ersap.engine.util.StreamingDataTypes;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class VTPWriterEngine extends AbstractEventWriterService<FileWriter> {
    @Override
    protected FileWriter createWriter(Path file, JSONObject opts)
            throws EventWriterException {
        try {
            return new FileWriter(file.toString());
        } catch (IOException e) {
            throw new EventWriterException(e);
        }
    }

    @Override
    protected void closeWriter() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void writeEvent(Object event) throws EventWriterException {
    }

    @Override
    protected EngineDataType getDataType() {
        return StreamingDataTypes.VTP_G;
    }
}
