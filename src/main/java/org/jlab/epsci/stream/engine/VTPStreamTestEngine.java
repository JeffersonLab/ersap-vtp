package org.jlab.epsci.stream.engine;

import org.jlab.epsci.ersap.base.ErsapUtil;
import org.jlab.epsci.ersap.engine.Engine;
import org.jlab.epsci.ersap.engine.EngineData;
import org.jlab.epsci.ersap.engine.EngineDataType;
import org.jlab.epsci.stream.engine.util.StreamingDataTypes;
import org.jlab.epsci.stream.vtp.VReceiver;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class VTPStreamTestEngine implements Engine {
    private static final String PRINT_INTERVAL = "print-interval";
    private boolean print;
    @Override
    public EngineData configure(EngineData input) {
        System.out.println("VTPStreamTestEngine engine configure...");

        if (input.getMimeType().equalsIgnoreCase(EngineDataType.JSON.mimeType())) {
            String source = (String) input.getData();
            JSONObject data = new JSONObject(source);
            if (data.has(PRINT_INTERVAL)) {
                int pi = data.getInt(PRINT_INTERVAL);
                // Timer for measuring and printing statistics.
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        print = true;
                    }
                }, 0, pi * 1000);

            }
        }
            return null;
    }

    @Override
    public EngineData execute(EngineData input) {
        if(print) {
            System.out.println("========================");
            System.out.println("Composition  = " + input.getComposition());
            System.out.println("MimeType     = " + input.getMimeType());
            System.out.println("ComId        = " + input.getCommunicationId());
            ByteBuffer data = (ByteBuffer) input.getData();
            System.out.println("-------------------------");
            System.out.println("Time         = " + data.getLong());
            System.out.println("Crate        = " + data.getInt());
            System.out.println("Slot         = " + data.getInt());
            System.out.println("Channel      = " + data.getInt());
            System.out.println("Charge       = " + data.getInt());
            System.out.println();
            print = false;
        }
        return input;
    }

    @Override
    public EngineData executeGroup(Set<EngineData> inputs) {
        return inputs.iterator().next();
    }

    @Override
    public Set<EngineDataType> getInputDataTypes() {
        return ErsapUtil.buildDataTypes(StreamingDataTypes.VTP_G,
                EngineDataType.JSON);
    }

    @Override
    public Set<EngineDataType> getOutputDataTypes() {
        return ErsapUtil.buildDataTypes(StreamingDataTypes.VTP_G);
    }

    @Override
    public Set<String> getStates() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Test VTP Stream Engine";
    }

    @Override
    public String getVersion() {
        return "0.0";
    }

    @Override
    public String getAuthor() {
        return "Vardan Gyurjyan";
    }

    @Override
    public void reset() {
    }

    @Override
    public void destroy() {
    }
}
