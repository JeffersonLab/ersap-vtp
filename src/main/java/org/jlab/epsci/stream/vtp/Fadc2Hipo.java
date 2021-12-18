package org.jlab.epsci.stream.vtp;

import j4np.hipo5.data.*;
import j4np.hipo5.data.Schema.SchemaBuilder;
import j4np.hipo5.io.*;

import java.util.List;

public class Fadc2Hipo {
    private final Schema rawSchema;
    private final HipoWriter w;

    public Fadc2Hipo(String fileName) {
        SchemaBuilder builder = new SchemaBuilder("raw::data", 1200, 1);
        builder.addEntry("crate", "I", "crate number");
        builder.addEntry("slot", "I", "slot number");
        builder.addEntry("channel", "I", "channel number");
        builder.addEntry("charge", "I", "accumulated charge");
        builder.addEntry("time", "L", "time of the hit");
        rawSchema = builder.build();
//        rawSchema.show();

        w = new HipoWriter();
        w.getSchemaFactory().addSchema(rawSchema);
        w.getSchemaFactory().show();

        w.open(fileName);
    }

    public synchronized void evtWrite(List<VAdcHit> hits) {
        Event event = new Event();
        Bank rBank = new Bank(rawSchema, hits.size());
        int row = 0;
        for (VAdcHit hit : hits) {
            rBank.putInt("crate", row, hit.getCrate());
            rBank.putInt("slot", row, hit.getSlot());
            rBank.putInt("channel", row, hit.getChannel());
            rBank.putInt("charge", row, hit.getCharge());
            rBank.putLong("time", row, hit.getTime());
            row++;
        }
//        rBank.show();
        event.reset();
        event.write(rBank);
//        event.scanShow();
        w.addEvent(event);
    }

    public void close() {
        w.close();
    }
}
