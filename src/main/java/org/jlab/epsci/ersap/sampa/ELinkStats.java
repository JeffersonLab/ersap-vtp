package org.jlab.epsci.ersap.sampa;

import java.util.Arrays;

public class ELinkStats {
    private int[] syncFoundCount = new int[28];
    private int[] syncLostCount = new int[28];
    private int[] syncCount = new int[28];
    private int[] heartBeatCount = new int[28];
    private int[] dataHeaderCount = new int[28];
    private int[] dataChannelCount = new int[160];

    public int[] getSyncFoundCount() {
        return syncFoundCount;
    }

    public int[] getSyncLostCount() {
        return syncLostCount;
    }

    public int[] getSyncCount() {
        return syncCount;
    }

    public int[] getHeartBeatCount() {
        return heartBeatCount;
    }

    public int[] getDataHeaderCount() {
        return dataHeaderCount;
    }

    public int[] getDataChannelCount() {
        return dataChannelCount;
    }

    public void init(){
        Arrays.fill(syncFoundCount, 0);
        Arrays.fill(syncLostCount, 0);
        Arrays.fill(syncCount, 0);
        Arrays.fill(heartBeatCount, 0);
        Arrays.fill(dataHeaderCount, 0);
        Arrays.fill(dataChannelCount, 0);
    }

    public void print() {
        // print elink stats
        int channel;

        System.out.println();

        for (int ii = 0; ii < 28; ii++) {
            System.out.println("-------------------------------- elink = " + ii + " ---------------------------------------- \n");
            System.out.println(" sync count = " + getSyncCount()[ii]
                    + "  sync found count = " + getSyncFoundCount()[ii]
                    + "  sync lost count = " + getSyncLostCount()[ii] + "\n");
            System.out.println(" data header count = " + getDataHeaderCount()[ii]
                    + "  heartbeat count = " + getHeartBeatCount()[ii] + "\n");
        }

        System.out.println("\n --------------------------------------------- channel counts -----------------------------------------------");

        for (int chip = 0; chip < 5; chip++) {
            for (int ch = 0; ch < 32; ch++) {
                channel = chip * 32 + ch;
                if ((channel % 16) == 0)
                    System.out.print("\n" + "chan " + channel + ": ");
                if ((channel % 16) == 8)
                    System.out.print("  ");
                System.out.print(getDataChannelCount()[channel] + " ");
                if (channel == 79)
                    System.out.println();
            }
        }
        System.out.println("\n------------------------------------------------------------------------------------------------------------\n\n");
    }
}
