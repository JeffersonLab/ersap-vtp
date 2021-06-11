package org.jlab.epsci.ersap.engine;

import org.jlab.clara.base.ClaraUtil;
import org.jlab.clara.engine.Engine;
import org.jlab.clara.engine.EngineData;
import org.jlab.clara.engine.EngineDataType;
import org.jlab.epsci.ersap.engine.util.StreamingDataTypes;

import java.nio.ByteBuffer;
import java.util.Set;

public class VTPStreamTestEngine implements Engine {
    @Override
    public EngineData configure(EngineData input) {
        System.out.println("VTPStreamTestEngine engine configure...");
        return input;
    }

    @Override
    public EngineData execute(EngineData input) {
        System.out.println("========================");
        System.out.println("Composition  = " + input.getComposition());
        System.out.println("MimeType     = " + input.getMimeType());
        System.out.println("ComId        = " + input.getCommunicationId());
        ByteBuffer data = (ByteBuffer)input.getData();
        System.out.println("-------------------------");
        System.out.println("Time         = " + data.getLong());
        System.out.println("Crate        = " + data.getInt());
        System.out.println("Slot         = " + data.getInt());
        System.out.println("Channel      = " + data.getInt());
        System.out.println("Charge       = " + data.getInt());
        System.out.println("");
        return input;
    }

    @Override
    public EngineData executeGroup(Set<EngineData> inputs) {
        return inputs.iterator().next();
    }

    @Override
    public Set<EngineDataType> getInputDataTypes() {
        return ClaraUtil.buildDataTypes(StreamingDataTypes.VTP_G,
                EngineDataType.JSON);
    }

    @Override
    public Set<EngineDataType> getOutputDataTypes() {
        return ClaraUtil.buildDataTypes(StreamingDataTypes.VTP_G);
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
