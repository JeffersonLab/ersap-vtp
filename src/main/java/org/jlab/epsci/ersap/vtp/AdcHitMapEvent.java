package org.jlab.epsci.ersap.vtp;

import java.util.ArrayList;
import java.util.List;

public class AdcHitMapEvent {

    private List<Long> time = new ArrayList<>();
    private List<Integer> crate = new ArrayList<>();
    private List<Integer> slot = new ArrayList<>();
    private List<Integer> channel = new ArrayList<>();
    private List<Integer> charge = new ArrayList<>();

    public void reset() {
        crate.clear();
        slot.clear();
        channel.clear();
        charge.clear();
    }

    public int evtSize() {
        if (time.size() == crate.size() &&
                crate.size() == slot.size() &&
                slot.size() == channel.size() &&
                channel.size() == charge.size()) {
            return crate.size();
        } else {
            return -1;
        }
    }

    public void add(long time, int crate, int slot, int channel, int charge) {
        this.time.add(time);
        this.crate.add(crate);
        this.slot.add(slot);
        this.channel.add(channel);
        this.charge.add(charge);
    }

    public int getCrate(int index) {
        return crate.get(index);
    }

    public int getSlot(int index) {
        return slot.get(index);
    }

    public int getChannel(int index) {
        return channel.get(index);
    }

    public int getCharge(int index) {
        return charge.get(index);
    }

    public long getTime(int index) {
        return time.get(index);
    }

}
