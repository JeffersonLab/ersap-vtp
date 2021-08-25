package org.jlab.epsci.stream.engine;

import org.jlab.epsci.ersap.engine.EngineDataType;
import org.jlab.epsci.ersap.std.services.AbstractEventWriterService;
import org.jlab.epsci.ersap.std.services.EventWriterException;
import org.jlab.epsci.stream.engine.util.SampaDasType;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class SMPWriterEngine extends AbstractEventWriterService<FileWriter> {
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
        return SampaDasType.SAMPA_DAS;
    }
}
