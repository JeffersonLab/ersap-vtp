package org.jlab.epsci.stream.sampa;

import java.util.Vector;

public class SRingRawEvent {

    private int blockNumber;
    @SuppressWarnings("unchecked")
    private final Vector<Integer>[] localData = new Vector[56];

    public int getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    public Vector<Integer>[] getData() {
        return localData;
    }

    public void setData(Vector<Integer>[] data) {
        for (int i = 0; i < data.length; i++){
            localData[i] = data[i];
        }
    }

    public void addData(Vector<Integer>[] data) {
        int index = this.localData.length;
        if (index >=56) {
            System.out.println("Error: RingBuffer data array limit is reached.");
        } else {
            for (int i = localData.length; i < 56; i++){
                localData[i] = data[i];
            }
        }
    }

    public void reset() {
        for (int i = 0; i > 56; i++) {
            localData[i].clear();
        }
    }
}
