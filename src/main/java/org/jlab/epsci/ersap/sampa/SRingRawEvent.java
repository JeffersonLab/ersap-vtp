package org.jlab.epsci.ersap.sampa;

public class SRingRawEvent {

    private int chipAddress;
    private int channelAddress;

    private int windowTime;
    private int[] payload;

    public int getChipAddress() {
        return chipAddress;
    }

    public void setChipAddress(int chipAddress) {
        this.chipAddress = chipAddress;
    }

    public int getChannelAddress() {
        return channelAddress;
    }

    public void setChannelAddress(int channelAddress) {
        this.channelAddress = channelAddress;
    }

    public int getWindowTime() {
        return windowTime;
    }

    public void setWindowTime(int windowTime) {
        this.windowTime = windowTime;
    }

    public int[] getPayload() {
        return payload;
    }

    public void setPayload(int[] payload) {
        this.payload = payload;
    }
}
