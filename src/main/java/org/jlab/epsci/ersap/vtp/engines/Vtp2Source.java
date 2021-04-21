package org.jlab.epsci.ersap.vtp.engines;


import org.jlab.clara.engine.EngineDataType;
import org.jlab.clara.std.services.EventReaderException;
import org.jlab.epsci.ersap.vtp.VTPTwoStreamAggregatorDecoder;
import org.json.JSONObject;

import java.nio.ByteOrder;

//public class Vtp2Source extends AbstractTwoStreamSourceService<VTPTwoStreamAggregatorDecoder> {
//
//private VTPTwoStreamAggregatorDecoder vtp_ad;
//    @Override
//    protected VTPTwoStreamAggregatorDecoder createStreamSource(int port1, int port2, JSONObject opts) throws EventReaderException {
//        try {
//            vtp_ad = new VTPTwoStreamAggregatorDecoder(port1, port2);;
//            return vtp_ad;
//        } catch (Exception e) {
//            throw new StreamSourceException(e);
//        }
//    }
//
//    @Override
//    protected void closeStreamSource() {
//      vtp_ad.close();
//    }
//
//
//    @Override
//    protected ByteOrder readByteOrder() throws EventReaderException {
//        return ByteOrder.LITTLE_ENDIAN;
//    }
//
//    @Override
//    protected void streamStart() {
//        vtp_ad.go();
//    }
//
//
//    @Override
//    protected EngineDataType getDataType() {
//        return null;
//    }
//
//    @Override
//    protected Object getStreamUnit() throws StreamSourceException {
//        try {
//            return vtp_ad.getDecodedEvent();
//        } catch (Exception e) {
//            throw new StreamSourceException(e);
//        }
//    }
//}

