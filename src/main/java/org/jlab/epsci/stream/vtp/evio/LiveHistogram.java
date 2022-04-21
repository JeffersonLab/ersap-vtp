package org.jlab.epsci.stream.vtp.evio;

import twig.data.H1F;
import twig.graphics.TGDataCanvas;

import javax.swing.*;
import java.util.Random;

/**
 * Copyright (c) 2021, Jefferson Science Associates, all rights reserved.
 * See LICENSE.txt file.
 * Thomas Jefferson National Accelerator Facility
 * Experimental Physics Software and Computing Infrastructure Group
 * 12000, Jefferson Ave, Newport News, VA 23606
 * Phone : (757)-269-7100
 *
 * @author gurjyan on 4/20/22
 * @project ersap-vtp
 */
public class LiveHistogram {
    public static void main(String[] args) {

        JFrame frame = new JFrame("Canvas");
        TGDataCanvas c = new TGDataCanvas();

        frame.add(c);
        frame.setSize(600, 600);
        frame.setVisible(true);

        c.initTimer(600);
        H1F h = new H1F("h", 100, -3.0, 3.0);

        c.region().draw(h);
        Random r = new Random();
        while (true) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            for (int k = 0; k < 10; k++) h.fill(r.nextGaussian());
        }
    }
}
