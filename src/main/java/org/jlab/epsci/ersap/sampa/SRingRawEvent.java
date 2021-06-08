package org.jlab.epsci.ersap.sampa;

import java.util.Vector;

public class SRingRawEvent {

    private int blockNumber;
    private Vector<Integer>[] data = new Vector[28];

    public int getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    public Vector<Integer>[] getData() {
        return data;
    }

    public void setData(Vector<Integer>[] data) {
        this.data = data;
    }

    public void reset() {
        for (int i = 0; i > 28; i++) {
            data[i].clear();
        }
    }
}
