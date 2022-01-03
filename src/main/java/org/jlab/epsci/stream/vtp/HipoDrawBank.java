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

    public static final int A = 8;
    public static final int B = 9;
    public static final int C = 10;
    public static final int D = 14;
    public static final int E = 15;
    public static final int F = 16;

    public static void main(String[] args) {
        HipoReader r = new HipoReader();
        r.open(args[0]);

        Bank raw = r.getBank("raw::data");
        Event event = new Event();

        H1F hl = new H1F("h1", 100, 0.0, 8000.0);
        H1F hc = new H1F("h2", 100, 0.0, 8000.0);
        H1F hr = new H1F("h2", 100, 0.0, 8000.0);
        H1F ha = new H1F("h1", 100, 0.0, 8000.0);
        H1F hb = new H1F("h2", 100, 0.0, 8000.0);
        H1F hcc = new H1F("h2", 100, 0.0, 8000.0);
        H1F hd = new H1F("h1", 100, 0.0, 8000.0);
        H1F he = new H1F("h2", 100, 0.0, 8000.0);
        H1F hf = new H1F("h2", 100, 0.0, 8000.0);
//        H2F h2 = new H2F("h2", 112, 0.0, 112.0, 60, 0.0, 500.0);

        List<Integer> a = new ArrayList<>();
        List<Integer> b = new ArrayList<>();
        List<Integer> cc = new ArrayList<>();

        List<Integer> left = new ArrayList<>();
        List<Integer> center = new ArrayList<>();
        List<Integer> right = new ArrayList<>();

        List<Integer> d = new ArrayList<>();
        List<Integer> e = new ArrayList<>();
        List<Integer> f = new ArrayList<>();

        while (r.hasNext()) {
            left.clear();
            center.clear();
            right.clear();
            a.clear();
            b.clear();
            cc.clear();
            d.clear();
            e.clear();
            f.clear();

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
                    } else if (channel == A) {
                        a.add(charge);
                    } else if (channel == B) {
                        b.add(charge);
                    } else if (channel == C) {
                        cc.add(charge);
                    } else if (channel == D) {
                        d.add(charge);
                    } else if (channel == E) {
                        e.add(charge);
                    } else if (channel == F) {
                        f.add(charge);
                    }
                }
            }

            if (left.size() == 1
                    && center.size() == 1
                    && right.size() == 1) {
                if ((center.get(0) > left.get(0))
                        && (center.get(0) > right.get(0))) {
                    hl.fill(left.get(0));
                    hc.fill(center.get(0));
                    hr.fill(right.get(0));
                    if (!a.isEmpty()) ha.fill(a.get(0));
                    if (!b.isEmpty()) hb.fill(b.get(0));
                    if (!cc.isEmpty()) hcc.fill(cc.get(0));
                    if (!d.isEmpty()) hd.fill(d.get(0));
                    if (!e.isEmpty()) he.fill(e.get(0));
                    if (!f.isEmpty()) hf.fill(f.get(0));
                }
            }

//                h2.fill(comp, tdc);

        }

        TGCanvas c = new TGCanvas(800, 500);
        c.view().divide(3, 3);
        c.view().region(0).draw(ha);
        c.view().region(1).draw(hb);
        c.view().region(2).draw(hcc);
        c.view().region(0).draw(hl);
        c.view().region(1).draw(hc);
        c.view().region(2).draw(hr);
        c.view().region(0).draw(hd);
        c.view().region(1).draw(he);
        c.view().region(2).draw(hf);
        c.repaint();
    }
}
