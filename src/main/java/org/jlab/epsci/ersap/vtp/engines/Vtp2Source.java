package org.jlab.epsci.ersap.vtp.engines;


import org.jlab.clara.base.ClaraUtil;
import org.jlab.clara.engine.EngineDataType;
import org.jlab.clara.std.services.AbstractEventReaderService;
import org.jlab.clara.std.services.EventReaderException;
import org.jlab.epsci.ersap.vtp.TwoStreamAggregator;
import org.json.JSONObject;

import java.nio.ByteOrder;
import java.nio.file.Path;

public class Vtp2Source extends AbstractEventReaderService<TwoStreamAggregator> {

    private static final String CONF_PORT_1 = "port1";
    private static final String CONF_PORT_2 = "port2";
    private static final String CONF_NEVENT = "nevents";

    private int nEvents;

    private static int getPort1(JSONObject opts) {
        return opts.has(CONF_PORT_1) ? opts.getInt(CONF_PORT_1) : 6000;
    }

    private static int getPort2(JSONObject opts) {
        return opts.has(CONF_PORT_2) ? opts.getInt(CONF_PORT_2) : 6001;
    }

    private static int getNumberOfEvents(JSONObject opts) {
        return opts.has(CONF_NEVENT) ? opts.getInt(CONF_NEVENT) : Integer.MAX_VALUE;
    }


    @Override
    protected TwoStreamAggregator createReader(Path file, JSONObject opts) throws EventReaderException {
        nEvents = getNumberOfEvents(opts);
        try {
            return new TwoStreamAggregator(getPort1(opts), getPort2(opts));
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
        return nEvents;
    }

    @Override
    protected ByteOrder readByteOrder() throws EventReaderException {
        return ByteOrder.LITTLE_ENDIAN;
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

