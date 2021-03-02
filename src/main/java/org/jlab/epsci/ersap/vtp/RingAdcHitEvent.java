package org.jlab.epsci.ersap.vtp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RingAdcHitEvent {

    private Map<Long, List<AdcHit>> hitMap = new HashMap<>();

    public void reset(){
        hitMap.clear();
    }

    public void addHit (Long time, AdcHit hit){
        if ( hitMap.containsKey(time)) {
            hitMap.get(time).add(hit);
        } else {
            List<AdcHit> l = new ArrayList<>();
            l.add(hit);
            hitMap.put(time,l);
        }
    }

    public Map<Long, List<AdcHit>> getHitMap(){
        return hitMap;
    }


}
