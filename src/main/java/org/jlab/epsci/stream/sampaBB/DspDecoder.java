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
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@SuppressWarnings("unchecked")

public class DspDecoder implements SampaDecoder {

    /** If true, print out debug info. */
    private boolean verbose;

    /** Link (0-27) to debug when in verbose mode. */
    int eLinkToDebug = 2;

    /** Object in which to keep statistics about each link. */
    private final ELinkStats eLinkStats = new ELinkStats();

    // static variables for 28 elink serial streams
    private final long[] shiftReg      = new long[28];
    private final int[] syncFound      = new int[28];
    private final int[] dataHeader     = new int[28];
    private final int[] headerBitCount = new int[28];
    private final int[] dataBitCount   = new int[28];
    private final int[] dataWordCount  = new int[28];
    private final int[] dataCount      = new int[28];
    private final int[] numWords       = new int[28];

    // There are 28 eLinks. Each link has either 2 or 3 channels' data on it.
    // There are 80 channels coming over the 28 links representing 2.5 of the 5 SAMPA chips.
    // There are 2 groups of 80 for a total of 160 channels representing all 5 chips.
    // Each of the 2, 80 channel groups go to one of 2 high speed serial links.
    //
    // The data from each link is reformatted into 2, 32-bit header words, followed by data words.
    // Each data word starts with the 10 bit ADC value, # of ADC samples, or Time. The other bits
    // contain a packet word number and 3 bits identifying it as a data word (0).

    // Try switching over from ArrayLists or Vectors to ByteBuffers
    private final ByteBuffer[] eLinkDataTemp = new ByteBuffer[28];

    /** Sync pattern. */
    private static final long syncHeaderPattern = 0x1555540F00113L;

    private static final int N_BLOCK = 1;
    private static final int frames_in_block = 2000 * N_BLOCK;

    public long frameCount;
    public int blockFrameCount;
    public int block_count;
    public int block_header;
    public int data;
    public int numData;


    public DspDecoder() {
        this(false);
    }


    public DspDecoder(boolean verbose) {
        this.verbose = verbose;
        eLinkStats.init(); // not necessary, but anyways

        for (int i = 0; i < 28; i++) {
            // 2 headers + (1024 samples + times + counts) * 32 bits each -> max < 65k
            // This allocates about 1MB, but eliminates having to always check if we
            // need to expand these buffers (and then expand them).
            eLinkDataTemp[i] = ByteBuffer.allocate(65536);
        }
    }


    public void incrementFrameCount() {frameCount++;}
    public void incrementBlockFrameCount() {blockFrameCount++;}
    public void clearBlockFrameCount() {blockFrameCount = 0;}


    public void decodeSerial(int[] gbt_frame, SRingRawEvent rawEvent) {
        int bitValue;
        int dataWord;
        int pkt;
        int hadd;
        int chadd;
        int bxCount;
        int hamming;
        int parity;
        int dataParity;
        int head1;
        int head2;
        int dataValue;
        int gFrameWord;
        int ii_min;
        int ii_max;
        int fec_channel;
        boolean match;

        // Loop thru all 28 eLinks ...
        for (int eLink = 0; eLink < 28; eLink++) {

            if (eLink < 8) {
                gFrameWord = gbt_frame[0];
            } else if ((eLink >= 8) && (eLink < 16)) {
                gFrameWord = gbt_frame[1];
            } else if ((eLink >= 16) && (eLink < 24)) {
                gFrameWord = gbt_frame[2];
            } else {
                gFrameWord = gbt_frame[3];
            }
            ii_min = (eLink % 8) * 4;
            ii_max = ii_min + 3;

            // find sync header - this will run until first sync packet header is found
            // sync packet header pattern
            if (syncFound[eLink] == 0) {
                for (int ii = ii_max; ii >= ii_min; ii--) {
                    // elink (4 bits per frame)
                    bitValue = (gFrameWord >>> ii) & 1;
                    // bitValue = (gFrameWord & (0x00000001 << ii)) >> ii;

                    if (bitValue == 1) {
                        if (verbose && (eLink == eLinkToDebug)) {
                            System.out.println("-> " + ii + " " + Long.toHexString(shiftReg[eLink]));
                        }

                        shiftReg[eLink] = shiftReg[eLink] | 0x0004000000000000L; // set bit 50 in shiftReg

                        if (verbose && (eLink == eLinkToDebug)) {
                            System.out.println("-> " + ii + " " + Long.toHexString(shiftReg[eLink]));
                        }
                    }
                    // Carl, fix bug, needed a >>> instead of >>
                    shiftReg[eLink] = shiftReg[eLink] >>> 1;

                    if (verbose && (eLink == eLinkToDebug)) {
                        System.out.println("DDD-> " + ii + " " + Integer.toHexString(gFrameWord) + " " + Integer.toHexString(bitValue));
                        System.out.println("elink = " + eLink + " shiftReg = " + Long.toHexString(shiftReg[eLink]));
                    }
                    if (syncFound[eLink] != 0) {
                        // when sync found count remaining bits of frame for next header
                        headerBitCount[eLink]++;
                    }
                    if (shiftReg[eLink] == syncHeaderPattern) {
                        // check if sync packet header detected
                        syncFound[eLink] = 1;
                        eLinkStats.getSyncFoundCount()[eLink]++;
                        eLinkStats.getSyncCount()[eLink]++;
                        headerBitCount[eLink] = 0;
                        if (verbose && (eLink == eLinkToDebug)) {
                            System.out.println("DDD:  ****************|| SYNC HEADER  elink = " + eLink + " ||****************** ");
                        }
                    }
                }
                if (syncFound[eLink] != 0) {
                    // print headerBitCount after frame where sync packet found
                    if (verbose && (eLink == eLinkToDebug)) {
                        System.out.println("DDD: SyncPacket found headerBitCount = " + headerBitCount[eLink]);
                    }
                }
            }
            else if (dataHeader[eLink] == 0) {
                // runs only after first sync packet header has been found
                // we find NEXT header here
                for (int ii = ii_max; ii >= ii_min; ii--) {
                    // elink 0 (4 bits per frame)
                    bitValue = (gFrameWord >>> ii) & 1;
                    if (bitValue == 1) {
                        shiftReg[eLink] = shiftReg[eLink] | 0x0004000000000000L;        // set bit 50 in shiftReg
                    }
                    shiftReg[eLink] = shiftReg[eLink] >>> 1;

                    if (dataHeader[eLink] > 0) {
                        // AFTER data header is found count remaining bits of frame as data bits
                        dataBitCount[eLink]++;
                    } else {
                        // count frame bits as header bits not data type
                        headerBitCount[eLink]++;
                    }
//          -----------------------------------------------------------------------
                    if (headerBitCount[eLink] == 50) {
                        // next packet header - decode
                        if (shiftReg[eLink] == syncHeaderPattern) {
                            // sync header
                            eLinkStats.getSyncCount()[eLink]++;
                            headerBitCount[eLink] = 0;

                            if (verbose && (eLink == eLinkToDebug)) {
                                System.out.println("DDD: **************** SYNC HEADER  elink = " + eLink +
                                        " shiftReg = 0x" + Long.toHexString(shiftReg[eLink]) +
                                        " syncCount = " + eLinkStats.getSyncCount()[eLink]);
                            }
                        } else {
                            // non-sync packet header - identify type
                            pkt             = (int) ((shiftReg[eLink] >>> 7) & 0x7);
                            numWords[eLink] = (int) ((shiftReg[eLink] >>> 10) & 0x3FF);
                            hadd            = (int) ((shiftReg[eLink] >>> 20) & 0xF);
                            chadd           = (int) ((shiftReg[eLink] >>> 24) & 0x1F);
                            bxCount         = (int) ((shiftReg[eLink] >>> 29) & 0xFFFFF);
                            hamming         = (int) ((shiftReg[eLink]) & 0x3F);
                            dataParity      = (int) ((shiftReg[eLink] >>> 49) & 0x1);
                            parity          = (int) ((shiftReg[eLink] >>> 6) & 0x1);

                            // Carl, this is different than Ed's, Ed's code looks like an error
                            if ((pkt == 0) && (numWords[eLink] == 0) && (chadd == 0x15)) {
                                // heartbeat packet (NO payload) - push into output stream
                                eLinkStats.getHeartBeatCount()[eLink]++;
                                headerBitCount[eLink] = 0;
                                head1 = 0xA0000000 | (bxCount << 9) | (chadd << 4) | hadd;
                                head2 = 0x40000000 | (parity << 23) | (hamming << 17) | (dataParity << 16) | (numWords[eLink] << 3) | pkt;

                                if (verbose && (eLink == eLinkToDebug)) {
                                    System.out.println("DDD: **************** HEARTBEAT HEADER  elink = " + eLink
                                            + " shiftReg = 0x" + Long.toHexString(shiftReg[eLink]) +
                                            " heartBeatCount = " + eLinkStats.getHeartBeatCount()[eLink]);
                                }
                            }
                            else if (pkt == 4) {
                                // initially require only NORMAL data packet headers
                                // check consistency of data header - verify that 'hadd' and chadd' are consistent with 'eLink' number
                                match = matchDataHeader(eLink, hadd, chadd);

                                if (match) {
                                    // header consistent with data header
                                    dataCount[eLink]++;
                                    eLinkStats.getDataHeaderCount()[eLink]++;
                                    fec_channel = (hadd * 32) + chadd;
                                    if ((fec_channel >= 0) && (fec_channel <= 159)) {
                                        eLinkStats.getDataChannelCount()[fec_channel]++;
                                    }
                                    else if (verbose) {
                                        System.out.println("DDD:  -------- ILLEGAL CHANNEL NUMBER  elink = " + eLink +
                                                " hadd = " + hadd + " chadd = " + chadd);
                                    }
                                    dataHeader[eLink] = 1;
                                    dataBitCount[eLink] = 0;
                                    dataWordCount[eLink] = 0;
                                    headerBitCount[eLink] = 0;
                                    head1 = 0xA0000000 | (bxCount << 9) | (chadd << 4) | hadd;
                                    head2 = 0x40000000 | (parity << 23) | (hamming << 17) | (dataParity << 16) | (numWords[eLink] << 3) | pkt;

                                    // push header into temporary storage vector
                                    eLinkDataTemp[eLink].putInt(head1);
                                    eLinkDataTemp[eLink].putInt(head2);

                                    if (verbose && (eLink == eLinkToDebug)) {
                                        System.out.println("DDD: **************** DATA HEADER  elink = " + eLink +
                                                " shiftReg = 0x" + Long.toHexString(shiftReg[eLink]) +
                                                " pkt = " + pkt +
                                                " dataCount = " + dataCount[eLink] +
                                                " numWords = " + numWords[eLink] +
                                                " hadd = " + hadd +
                                                " chadd = " + chadd +
                                                " bxCount = " + bxCount);
                                    }
                                } else {
                                    // inconsistent data header - force the finding of next sync header
                                    headerBitCount[eLink] = 0;
                                    syncFound[eLink] = 0;
                                    eLinkStats.getSyncLostCount()[eLink]++;
                                    if (verbose && (eLink == eLinkToDebug)) {
                                        System.out.println("DDD: UNRECOGNIZED HEADER  elink = " + eLink +
                                                " shiftReg = 0x" + Long.toHexString(shiftReg[eLink]) +
                                                " pkt = ");
                                    }
                                }
                            } else {
                                // 'unrecognized' header - force the finding of next sync header
                                headerBitCount[eLink] = 0;
                                syncFound[eLink] = 0;
                                eLinkStats.getSyncLostCount()[eLink]++;
                                if (verbose && (eLink == eLinkToDebug)) {
                                    System.out.println("DDD: -------- UNRECOGNIZED HEADER  elink = " + eLink +
                                            " shiftReg = 0x" + Long.toHexString(shiftReg[eLink]) +
                                            " pkt = " + pkt);
                                }
                            }
                        }
                    }
                    //-----------------------------------------------------------------------
                }
            }
            else if (dataHeader[eLink] > 0) {
                // runs only after data packet header has been found
                for (int ii = ii_max; ii >= ii_min; ii--) {
                    // elink (4 bits per frame)
                    // Carl, more efficient to do:
                    bitValue = (gFrameWord >>> ii) & 1;
                    // bitValue = (gFrameWord & (0x00000001 << ii)) >>> ii;
                    if (bitValue > 0)
                        shiftReg[eLink] = shiftReg[eLink] | 0x0004000000000000L;        // set bit 50 in shiftReg
                    shiftReg[eLink] = shiftReg[eLink] >>> 1;

                    if (dataHeader[eLink] > 0)       // count data word bits until data payload is exhausted
                        dataBitCount[eLink]++;
                    else                            // if payload is exhausted count remaining bits of frame for next header
                        headerBitCount[eLink]++;

                    if (dataBitCount[eLink] == 10) {
                        // print data word
                        dataWordCount[eLink]++;
                        dataWord = (int) ((shiftReg[eLink] >>> 40) & 0x3FF);
                        dataValue = (dataWordCount[eLink] << 16) | dataWord;
                        eLinkDataTemp[eLink].putInt(dataValue);
                        dataBitCount[eLink] = 0;

                        if (verbose && (eLink == eLinkToDebug)) {
                            System.out.println("DDD:  shiftReg = " + shiftReg[eLink] +
                                    " data = 0x" + Integer.toHexString(dataWord) +
                                    " data = " + dataWord +
                                    " dataWordCount = " + dataWordCount[eLink] +
                                    " elink = " + eLink);
                        }
                    }

                    if (dataWordCount[eLink] == numWords[eLink]) {
                        // Done with packet payload.
                        // Both header words and all packet data words have been stored in a temporary vector.
                        // This is done to assure that only complete packets appear in the output data stream.
                        // Now copy the temporary vector to the output stream.
                        int dataBytes = 4 * (numWords[eLink] + 2);
                        // Write directly into raw event's ByteBuffer.
                        // Expand it if necessary to handle all data.
                        ByteBuffer bb = rawEvent.expandBB(eLink, dataBytes);
                        System.arraycopy(eLinkDataTemp[eLink].array(), 0,
                                bb.array(), bb.position(), dataBytes);

                        // delete all data of temporary vector
                        eLinkDataTemp[eLink].clear();

                        // Reset
                        dataHeader[eLink] = 0;
                        headerBitCount[eLink] = 0;
                        dataBitCount[eLink] = 0;
                        dataWordCount[eLink] = 0;

                        if (verbose && (eLink == eLinkToDebug)) {
                            System.out.println("DDD: END OF DATA  elink = " + eLink);
                        }
                    }
                }
            }
        }
    }

    private boolean matchDataHeader(int eLink, int hadd, int chadd) {
        int chip_a;
        int chip_b;
        int chan_a;
        int chan_b;
        int chan_c;
        int chan_d;
        int chan_e;
        int chan_f;
        boolean chip_match;
        boolean channel_match;
        boolean match;

// check consistency of header - verify that 'hadd' and chadd' are consistent with 'eLink' number
        if (eLink < 11) {
            // eLink 0-10 are from chip 0 (link00) or chip 3 (link01)
            chip_a = 0;
            chip_b = 3;
        } else if (eLink < 22) {
            // eLink 11-21 are from chip 1 (link00) or chip 4 (link01)
            chip_a = 1;
            chip_b = 4;
        } else {
            // eLink 22-27 are from chip 2 (link00, link01)
            chip_a = 2;
            chip_b = 2;
        }

        if (eLink < 10) {
            // compare to 6 possible channel values due to complex chip 2 mapping
            chan_a = (eLink % 11) * 3;    // eLink 0 (ch 0,1,2), eLink 1 (ch 3,4,5), ... elink 9 (ch 27,28,29)
            chan_b = chan_a + 1;
            chan_c = chan_a + 2;
            chan_d = chan_a;
            chan_e = chan_b;
            chan_f = chan_c;
        } else if (eLink == 10) {
            // eLink 10 (ch 30,31)
            chan_a = 30;
            chan_b = 31;
            chan_c = 30;
            chan_d = 31;
            chan_e = 30;
            chan_f = 31;
        }
        // Carl, Ed's logic must be wrong! This if clause overwrites the  previous if statement.
        // Change   if   to   else if.
        // if (eLink < 21) {
        else if (eLink < 21) {
            // eLink 11 (ch 0,1,2), eLink 12 (ch 3,4,5), ... elink 20 (ch 27,28,29)
            chan_a = (eLink % 11) * 3;
            chan_b = chan_a + 1;
            chan_c = chan_a + 2;
            chan_d = chan_a;
            chan_e = chan_b;
            chan_f = chan_c;
        } else if (eLink == 21) {
            // eLink 21 (ch 30,31)
            chan_a = 30;
            chan_b = 31;
            chan_c = 30;
            chan_d = 31;
            chan_e = 30;
            chan_f = 31;
        } else if (eLink < 27) {
            // Link00:  eLink 22 (ch 0,1,2),    ... elink 26 (ch 12,13,14)
            // Link01:  eLink 22 (ch 15,16,17), ... elink 26 (ch 27,28,29)
            chan_a = (eLink % 22) * 3;
            chan_b = chan_a + 1;
            chan_c = chan_a + 2;
            chan_d = chan_a + 15;
            chan_e = chan_b + 15;
            chan_f = chan_c + 15;
        } else {
            // eLink 27 (ch 30,31)
            chan_a = 30;
            chan_b = 31;
            chan_c = 30;
            chan_d = 31;
            chan_e = 30;
            chan_f = 31;
        }

        chip_match = (hadd == chip_a) || (hadd == chip_b);
        channel_match = (chadd == chan_a) || (chadd == chan_b) || (chadd == chan_c)
                || (chadd == chan_d) || (chadd == chan_e) || (chadd == chan_f);

        match = chip_match && channel_match;
        return match;
    }

    public int getBlockCount() {return block_count;}

    public DecoderType getDecoderType() {return DecoderType.DSP;}

    public boolean isBlockComplete() {
        return blockFrameCount == frames_in_block;
    }


    /**
     * Write data values to output stream.
     * Note, even if specifying hex output, the values following
     * "eLink" and "num data" are always decimal.
     *
     * @param out output stream to write data to.
     * @param streamId id number of data stream.
     * @param rawEvent object that contains data to write.
     * @param hex if true, output values are in hexadecimal, else in decimal.
     */
    public void writeData(OutputStream out, int streamId, SRingRawEvent rawEvent, boolean hex) {

        boolean autoFlush = true;
        PrintWriter writer = new PrintWriter(out, autoFlush, Charset.forName("US_ASCII"));

        // TODO: Is there a better way to handle the printout when dealing with blocks ???
        if (blockFrameCount == frames_in_block) {
            // write block header to file
            block_count = block_count + 1;          // block count is 26 bits
            block_header = 0x20000000 | ((0xF & streamId) << 26) | (0x3FFFFFF & block_count);
            if (hex) {
                writer.println(Integer.toHexString(block_header));
            }
            else {
                writer.println(block_header);
            }

            // write vector data for block to file
            for (int jj = 0; jj < 28; jj++) {
                numData = rawEvent.getBB(jj).limit()/4;
                writer.print("  eLink = ");
                writer.print(jj);
                writer.print("   num data = ");
                writer.println(numData);

                if (numData > 0) {
                    for (int ii = 0; ii < numData; ii++) {
                        data = rawEvent.getBB(jj).getInt(ii);
                        if (hex) {
                            writer.print(Integer.toHexString(data));
                        }
                        else {
                            writer.print(data);
                        }
                        writer.print(" ");
                    }
                    writer.println();
                }
                else {
                    writer.println();
                }
            }
        }

        writer.flush();
    }


    public void printLinkStats() {eLinkStats.print();}
}
