package org.jlab.epsci.stream.sampaBB;

public enum DecoderType {
    DSP,
    DAS;

    boolean isDSP() {return this == DSP;}

    boolean isDAS() {return this == DAS;}
}
