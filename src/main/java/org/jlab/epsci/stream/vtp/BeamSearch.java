package org.jlab.epsci.stream.vtp;

import j4np.hipo5.data.Bank;
import j4np.hipo5.data.Event;
import j4np.hipo5.io.HipoReader;
import twig.data.H1F;
import twig.data.H2F;
import twig.graphics.TGCanvas;

import java.util.ArrayList;
import java.util.List;

public class BeamSearch {

    public static final int SLOT = 3;
    public static int leftChannel;
    public static int centerChannel;
    public static int rightChannel;

    public static int A = 8;
    public static int B = 9;
    public static int C = 10;
    public static int D = 14;
    public static int E = 15;
    public static int F = 16;

    public static void main(String[] args) {
        Boolean s = false;
        HipoReader r = new HipoReader();
        r.open(args[0]);
        leftChannel = Integer.parseInt(args[1]);
        centerChannel = Integer.parseInt(args[2]);
        rightChannel = Integer.parseInt(args[3]);
        A = leftChannel - 3;
        B = leftChannel - 2;
        C = leftChannel - 1;
        D = rightChannel + 1;
        E = rightChannel + 2;
        F = rightChannel + 3;
        if (args.length == 5) {
            s = true;
        }

        Bank raw = r.getBank("raw::data");
        Event event = new Event();

        H1F hl = new H1F("hl", 100, 0.0, 8000.0);
        H1F hc = new H1F("hc", 100, 0.0, 8000.0);
        H1F hr = new H1F("hr", 100, 0.0, 8000.0);
        H1F ha = new H1F("ha", 100, 0.0, 8000.0);
        H1F hb = new H1F("hb", 100, 0.0, 8000.0);
        H1F hcc = new H1F("hcc", 100, 0.0, 8000.0);
        H1F hd = new H1F("hd", 100, 0.0, 8000.0);
        H1F he = new H1F("he", 100, 0.0, 8000.0);
        H1F hf = new H1F("hf", 100, 0.0, 8000.0);
        H1F hsum = new H1F("sum", 100, 0.0, 20000.0);

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

            if (center.size() == 1
                    && left.size() <= 1
                    && right.size() <= 1
                    && a.size() <= 1
                    && b.size() <= 1
                    && cc.size() <= 1
                    && d.size() <= 1
                    && e.size() <= 1
                    && f.size() <= 1
            ) {
                if (((left.size() == 1) && (center.get(0) > left.get(0)))
                        && ((right.size() == 1) && (center.get(0) > right.get(0)))
                        && ((a.size() == 1) && (center.get(0) > a.get(0)))
                        && ((b.size() == 1) && (center.get(0) > b.get(0)))
                        && ((cc.size() == 1) && (center.get(0) > cc.get(0)))
                        && ((d.size() == 1) && (center.get(0) > d.get(0)))
                        && ((e.size() == 1) && (center.get(0) > e.get(0)))
//                        && ((f.size()==1) && (center.get(0) > f.get(0)))
                ) {
                    hc.fill(center.get(0));
                    if (left.size() == 1) hl.fill(left.get(0));
                    if (right.size() == 1) hr.fill(right.get(0));
                    if (a.size() == 1) ha.fill(a.get(0));
                    if (b.size() == 1) hb.fill(b.get(0));
                    if (cc.size() == 1) hcc.fill(cc.get(0));
                    if (d.size() == 1) hd.fill(d.get(0));
                    if (e.size() == 1) he.fill(e.get(0));
                    if (f.size() == 1) hf.fill(f.get(0));

                    int a1 = 0, l1 = 0, d1 = 0, b1 = 0, e1 = 0, cc1 = 0, r1 = 0, f1 = 0;

                    if (left.size() == 1 && left.get(0) >= 1000) l1 = left.get(0);
                    if (right.size() == 1 && right.get(0) >= 1000) r1 = right.get(0);
                    if (a.size() == 1 && a.get(0) >= 1000) a1 = a.get(0);
                    if (b.size() == 1 && b.get(0) >= 1000) b1 = b.get(0);
                    if (cc.size() == 1 && cc.get(0) >= 1000) cc1 = cc.get(0);
                    if (d.size() == 1 && d.get(0) >= 1000) d1 = d.get(0);
                    if (e.size() == 1 && e.get(0) >= 1000) e1 = e.get(0);
                    if (f.size() == 1 && f.get(0) >= 1000) f1 = f.get(0);
                    int sum = center.get(0) + l1 + r1 + a1 + b1 + cc1 + d1 + e1 + f1;
                    hsum.fill(sum);
                }
            }

//                h2.fill(comp, tdc);

        }
        TGCanvas c;
        if (s) {
            c = new TGCanvas(800, 500);
            c.view().divide(1, 1);
            c.view().region(0).draw(hsum);
        } else {
            c = new TGCanvas(1600, 1000);
            c.view().divide(3, 3);
            c.view().region(0).draw(ha);
            c.view().region(1).draw(hb);
            c.view().region(2).draw(hcc);
            c.view().region(3).draw(hl);
            c.view().region(4).draw(hc);
            c.view().region(5).draw(hr);
            c.view().region(6).draw(hd);
            c.view().region(7).draw(he);
            c.view().region(8).draw(hcc);
        }
        c.repaint();
    }
}
