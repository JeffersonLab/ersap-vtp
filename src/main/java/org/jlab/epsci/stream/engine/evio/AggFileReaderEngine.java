package org.jlab.epsci.stream.engine.evio;

import org.jlab.epsci.ersap.engine.EngineDataType;
import org.jlab.epsci.ersap.std.services.AbstractEventReaderService;
import org.jlab.epsci.ersap.std.services.EventReaderException;
import org.jlab.epsci.stream.engine.util.EvioDataType;
import org.jlab.epsci.stream.vtp.evio.AggFileOutputReader;
import org.json.JSONObject;

import java.nio.ByteOrder;
import java.nio.file.Path;

/**
 * Copyright (c) 2021, Jefferson Science Associates, all rights reserved.
 * See LICENSE.txt file.
 * Thomas Jefferson National Accelerator Facility
 * Experimental Physics Software and Computing Infrastructure Group
 * 12000, Jefferson Ave, Newport News, VA 23606
 * Phone : (757)-269-7100
 *
 * @author gurjyan on 4/21/22
 * @project ersap-vtp
 */
public class AggFileReaderEngine extends AbstractEventReaderService<AggFileOutputReader> {
    private static final String FILE = "agg_file";
    @Override
    protected AggFileOutputReader createReader(Path file, JSONObject opts) throws EventReaderException {
        if(opts.has(FILE)){
            String fName = opts.getString(FILE);
//            return new AggFileOutputReader(fName);
            return new AggFileOutputReader(file.toFile());
        } else {
            System.out.println("ERROR: Aggregator file is not defined.");
            return null;
        }
    }

    @Override
    protected void closeReader() {
      reader.close();
    }

    @Override
    protected int readEventCount() throws EventReaderException {
        return reader.getEventCount();
    }

    @Override
    protected ByteOrder readByteOrder() throws EventReaderException {
        return reader.getByteOrder();
    }

    @Override
    protected Object readEvent(int eventNumber) throws EventReaderException {
        return reader.nextEvent();
    }

    @Override
    protected EngineDataType getDataType() {
        return EvioDataType.EVIO;
    }
}
