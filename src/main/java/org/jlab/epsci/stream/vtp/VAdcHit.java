package org.jlab.epsci.stream.vtp;

public class VAdcHit {
    private int crate;
    private int slot;
    private int channel;
    private int charge;
    private long time;

    public VAdcHit(int crate, int slot, int channel, int charge, long time) {
        this.crate = crate;
        this.slot = slot;
        this.channel = channel;
        this.charge = charge;
        this.time = time;
    }

    public int getCrate() {
        return crate;
    }

    public int getSlot() {
        return slot;
    }

    public int getChannel() {
        return channel;
    }

    public int getCharge() {
        return charge;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "AdcHit{" +
                "crate=" + crate +
                ", slot=" + slot +
                ", channel=" + channel +
                ", charge=" + charge +
                ", time=" + time +
                '}';
    }
}
