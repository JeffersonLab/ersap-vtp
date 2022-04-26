package org.jlab.epsci.stream.vtp.evio;

import twig.data.H1F;
import twig.graphics.TGDataCanvas;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

    private Map<String, H1F> histograms = new HashMap<>();

    public LiveHistogram(String frameTitle, ArrayList<String> histTitles,
                         int gridSize, int frameWidth, int frameHeight,
                         int histBins, double histMin, double histMax) {

        JFrame frame = new JFrame(frameTitle);
        frame.setSize(frameWidth, frameHeight);
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(gridSize,gridSize));
        frame.getContentPane().add(panel);

        // create canvases with associated histograms,
        // and add them to the panel
        for(String s: histTitles){
            TGDataCanvas c = new TGDataCanvas();
            c.initTimer(600);
            H1F hist = new H1F(s, histBins, histMin, histMax);
            histograms.put(s, hist);
            c.region().draw(hist);
            panel.add(c);
        }
    }

    public void update (String name, int value) {
        if(histograms.containsKey(name)){
            histograms.get(name).fill(value);
        }
    }

    public static void main(String[] args) {

        JFrame frame = new JFrame("ERSAP");
        frame.setSize(1200, 600);
        /////
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2,2));
        frame.getContentPane().add(panel);
        /////

        TGDataCanvas c1 = new TGDataCanvas();
        TGDataCanvas c2 = new TGDataCanvas();
        TGDataCanvas c3 = new TGDataCanvas();
        TGDataCanvas c4 = new TGDataCanvas();

        ///
        panel.add(c1);
        panel.add(c2);
        panel.add(c3);
        panel.add(c4);
        ///

//        frame.add(c);
//        frame.setSize(600, 600);
        frame.setVisible(true);

        c1.initTimer(600);
        H1F h1 = new H1F("h1", 100, -3.0, 3.0);
        c1.region().draw(h1);

        c2.initTimer(600);
        H1F h2 = new H1F("h2", 100, -3.0, 3.0);
        c2.region().draw(h2);

        c3.initTimer(600);
        H1F h3 = new H1F("h3", 100, -3.0, 3.0);
        c3.region().draw(h3);

        c4.initTimer(600);
        H1F h4 = new H1F("h4", 100, -3.0, 3.0);
        c4.region().draw(h4);

        Random r = new Random();
        while (true) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            for (int k = 0; k < 10; k++) {
                h1.fill(r.nextGaussian());
                h2.fill(r.nextGaussian());
                h3.fill(r.nextGaussian());
                h4.fill(r.nextGaussian());
            }
        }
    }
}
