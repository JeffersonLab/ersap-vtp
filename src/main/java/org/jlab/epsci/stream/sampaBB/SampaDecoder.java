/*
 * Copyright (c) 2021, Jefferson Science Associates, all rights reserved.
 * See License.txt file.
 *
 * Thomas Jefferson National Accelerator Facility
 * Experimental Physics Software and Computing Infrastructure Group
 *
 * 12000, Jefferson Ave, Newport News, VA 23606
 * Phone : (757)-269-7100
 */

package org.jlab.epsci.stream.sampaBB;
import java.io.OutputStream;

public interface SampaDecoder {

    DecoderType getDecoderType();
    void decodeSerial(int[] gbt_frame, SRingRawEvent ringRawEvent) throws Exception;

    int getBlockCount();
    boolean isBlockComplete();

    void writeData(OutputStream out, int streamId, SRingRawEvent rawEvent, boolean hex);
    void printLinkStats();
}

