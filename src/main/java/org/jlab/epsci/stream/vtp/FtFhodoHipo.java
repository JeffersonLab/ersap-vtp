package org.jlab.epsci.stream.vtp;

//import org.jlab.jnp.hipo4.data.Schema.SchemaBuilder;
//import org.jlab.jnp.hipo4.data.Schema;
//import org.jlab.jnp.hipo4.data.Bank;
//import org.jlab.jnp.hipo4.data.Event;

public class FtFhodoHipo {

//    /**
//     * applies translation table to the digitized data to translate
//     * crate,slot channel to sector layer component.
//     */
//    public void translate(List<DetectorDataDgtz>  detectorData){
//
//        for(DetectorDataDgtz data : detectorData){
//
//            int crate    = data.getDescriptor().getCrate();
//            int slot     = data.getDescriptor().getSlot();
//            int channel  = data.getDescriptor().getChannel();
//            //if(crate==69){
//            //System.out.println(" MVT " + crate + " " + slot +
//            //  "  " + channel);
//            // }
//            boolean hasBeenAssigned = false;
//
//            for(String table : keysTrans){
//                IndexedTable  tt = translationManager.getConstants(runNumber, table);
//                DetectorType  type = DetectorType.getType(table);
//
//                if(tt.hasEntry(crate,slot,channel)==true){
//                    int sector    = tt.getIntValue("sector", crate,slot,channel);
//                    int layer     = tt.getIntValue("layer", crate,slot,channel);
//                    int component = tt.getIntValue("component", crate,slot,channel);
//                    int order     = tt.getIntValue("order", crate,slot,channel);
//
//                    /*if(crate>60&&crate<64){
//                        System.out.println(" SVT " + sector + " " + layer +
//                                "  " + component);
//                    }*/
//                    data.getDescriptor().setSectorLayerComponent(sector, layer, component);
//                    data.getDescriptor().setOrder(order);
//                    data.getDescriptor().setType(type);
//                    for(int i = 0; i < data.getADCSize(); i++) {
//                        data.getADCData(i).setOrder(order);
//                    }
//                    for(int i = 0; i < data.getTDCSize(); i++) {
//                        data.getTDCData(i).setOrder(order);
//                    }
//                }
//            }
//        }
//        //Collections.sort(detectorData);
//    }
//    public static void main(String[] args) {
//        /*
//        SchemaBuilder builder = new SchemaBuilder("FTCAL::adc",21000,11);
//
//        builder.addEntry("sector","B","")
//                .addEntry("layer","B","")
//                .addEntry("component","S","")
//                .addEntry("order","B","")
//                .addEntry("ADC","I","")
//                .addEntry("time","F","")
//                .addEntry("ped","S","")
//                .addEntry("integral","I","")
//                .addEntry("timestamp","L","");
//
//        SchemaBuilder builder2 = new SchemaBuilder("FTHODO::adc",21100,11);
//
//        builder2.addEntry("sector","B","")
//                .addEntry("layer","B","")
//                .addEntry("component","S","")
//                .addEntry("order","B","")
//                .addEntry("ADC","I","")
//                .addEntry("time","F","")
//                .addEntry("ped","S","");
//
//        Schema ftcalADC = builder.build();
//        Schema fthodoADC = builder2.build();
//
//        ftcalADC.show();
//        fthodoADC.show();
//
//        Event event = new Event();
//        Random rand = new Random();
//
//        for(int i = 0; i < 10; i++){
//            event.reset();
//            int nrows = rand.nextInt(6)+4;
//
//            Bank  bFTCAL = new Bank(ftcalADC,nrows);
//            Bank bFTHODO = new Bank(fthodoADC,nrows);
//
//            for(int r = 0 ; r < nrows; r++){
//                bFTCAL.putByte("sector",r, (byte) rand.nextInt(6));
//                bFTCAL.putByte("layer",r, (byte) 1);
//                bFTCAL.putShort("component",r, (short) rand.nextInt(242));
//                bFTCAL.putByte("order",r, (byte) 0); // This is because it only has ADC
//                bFTCAL.putInt("ADC",r, rand.nextInt(4096));
//                bFTCAL.putFloat("time",r, rand.nextFloat());
//                bFTCAL.putShort("ped",r, (short) rand.nextInt(95));
//            }
//
//            System.out.println(">>> event # " + i);
//            bFTCAL.show();
//            bFTHODO.show();
//            event.write(bFTCAL);
//            event.write(bFTHODO);
//            event.show();
//        }
//         */
//    }
}
