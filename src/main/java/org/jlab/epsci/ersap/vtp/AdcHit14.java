package org.jlab.epsci.ersap.vtp;

import java.math.BigInteger;

public record AdcHit14(int crate, int slot, int channel, int q, BigInteger time) {}
