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

    public void reset(){
        Arrays.fill(syncFoundCount, 0);
        Arrays.fill(syncLostCount, 0);
        Arrays.fill(syncCount, 0);
        Arrays.fill(heartBeatCount, 0);
        Arrays.fill(dataHeaderCount, 0);
        Arrays.fill(dataChannelCount, 0);
    }
}
