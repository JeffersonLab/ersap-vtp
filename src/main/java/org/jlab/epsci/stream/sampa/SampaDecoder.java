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

package org.jlab.epsci.stream.sampa;

public interface SampaDecoder {

    void decodeSerial(int eLink, int[] gbt_frame);

    void getBlockData(SRingRawEvent rawEvent);
    boolean isBlockComplete();

    void printBlockData(int steamId);
    void printLinkStats();
}

