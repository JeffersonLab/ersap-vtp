package org.jlab.epsci.stream.vtp;

import j4np.hipo5.data.Bank;
import j4np.hipo5.data.Event;
import j4np.hipo5.io.HipoReader;
import twig.data.H1F;
import twig.data.H2F;
import twig.graphics.TGCanvas;

import java.util.ArrayList;
import java.util.List;

public class HipoDrawBank {

    public static final int SLOT = 3;
    public static final int leftChannel = 11;
    public static final int centerChannel = 12;
    public static final int rightChannel = 13;

    public static void main(String[] args) {
        HipoReader r = new HipoReader();
        r.open(args[0]);

        Bank raw = r.getBank("raw::data");
        Event event = new Event();

        H1F hl = new H1F("h1", 100, 3000.0, 8000.0);
        H1F hc = new H1F("h2", 100, 3000.0, 8000.0);
        H1F hr = new H1F("h2", 100, 3000.0, 8000.0);
//        H2F h2 = new H2F("h2", 112, 0.0, 112.0, 60, 0.0, 500.0);

        List<Integer> left = new ArrayList<>();
        List<Integer> center = new ArrayList<>();
        List<Integer> right = new ArrayList<>();

        while (r.hasNext()) {
            r.nextEvent(event);
            event.read(raw);

            int nrows = raw.getRows();
            for (int j = 0; j < nrows; j++) {
                int slot = raw.getInt("slot", j);
                int channel = raw.getInt("channel", j);
                int charge = raw.getInt("charge", j);

                if (slot == SLOT) {
                    if (channel == leftChannel) {
                        left.add(charge);
                    } else if (channel == centerChannel) {
                        center.add(charge);
                    } else if (channel == rightChannel) {
                        right.add(charge);
                    }
                }
            }

            if(left.size() == 1
                    && center.size() ==1
                    && right.size() == 1) {
                if( (center.get(0) > left.get(0))
                && (center.get(0) > right.get(0)) ){
                    hl.fill(left.get(0));
                    hc.fill(center.get(0));
                    hr.fill(right.get(0));
                }
            }

//                h2.fill(comp, tdc);

        }

        TGCanvas c = new TGCanvas(800, 500);
        c.view().divide(3, 1);
        c.view().region(0).draw(hl);
        c.view().region(1).draw(hc);
        c.view().region(2).draw(hr);
        c.repaint();
    }
}
