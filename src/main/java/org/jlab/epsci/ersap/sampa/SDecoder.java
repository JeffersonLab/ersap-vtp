package org.jlab.epsci.ersap.sampa;

import java.util.Arrays;
import java.util.Vector;

@SuppressWarnings("unchecked")

public class SDecoder {

    private final ELinkStats eLinkStats = new ELinkStats();

    private final long[] shiftReg = new long[28]; // static variables for 28 elink serial streams
    private final int[] syncFound = new int[28];
    private final int[] dataHeader = new int[28];
    private final int[] headerBitCount = new int[28];
    private final int[] dataBitCount = new int[28];
    private final int[] dataWordCount = new int[28];
    private final int[] dataCount = new int[28];
    private final int[] numWords = new int[28];

    private Vector<Integer>[] eLinkDataTemp = new Vector[28];
    private Vector<Integer>[] eLinkData = new Vector[28];

    public SDecoder() {
        for (int i=0; i<28; i++){
            eLinkData[i] = new Vector<>();
            eLinkDataTemp[i] = new Vector<>();
        }

    }

    /**
     * Main decoder
     * @param eLink
     * @param gbt_frame
     */
    public void decodeSerial(int eLink, int[] gbt_frame) {
        long syncHeaderPattern = 0x1555540F00113L;
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
        int tempData;

        reset();

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
                bitValue = (gFrameWord & (0x00000001 << ii)) >>> ii;
                if (bitValue == 1) {
                    shiftReg[eLink] = shiftReg[eLink] | 0x0004000000000000L; // set bit 50 in shiftReg
                }
                shiftReg[eLink] = shiftReg[eLink] >>> 1;

                if (eLink == 2) {
                    System.out.println("DDD: elink = " + eLink +
                            " shiftReg = " +Long.toHexString(shiftReg[eLink]));
                }
                if (syncFound[eLink] != 0) {
                    // when sync found count remaining bits of frame for next header
                    headerBitCount[eLink] = headerBitCount[eLink] + 1;
                }
                if (shiftReg[eLink] == syncHeaderPattern) {
                    // check if sync packet header detected
                    syncFound[eLink] = 1;
                    eLinkStats.getSyncFoundCount()[eLink]++;
                    eLinkStats.getSyncCount()[eLink]++;
                    headerBitCount[eLink] = 0;
                    System.out.println("DDD:  ****************|| SYNC HEADER  elink = " + eLink + " ||****************** ");
                }
            }
            if (syncFound[eLink] != 0) {
                // print headerBitCount after frame where sync packet found
                System.out.println("DDD: SyncPacket found headerButCount = " + headerBitCount[eLink]);
            }
        } else if (dataHeader[eLink] == 0) {
            // runs only after first sync packet header has been found
            // we find NEXT header here
            for (int ii = ii_max; ii >= ii_min; ii--) {
                // elink 0 (4 bits per frame)
                bitValue = (gFrameWord & (0x00000001 << ii)) >>> ii;
                if (bitValue == 1) {
                    shiftReg[eLink] = shiftReg[eLink] | 0x0004000000000000L;        // set bit 50 in shiftReg
                }
                shiftReg[eLink] = shiftReg[eLink] >>> 1;

                if (dataHeader[eLink] == 1) {
                    // AFTER data header is found count remaining bits of frame as data bits
                    dataBitCount[eLink] = dataBitCount[eLink] + 1;
                } else {
                    // count frame bits as header bits not data type
                    headerBitCount[eLink] = headerBitCount[eLink] + 1;
                }
//          -----------------------------------------------------------------------
                if (headerBitCount[eLink] == 50) {
                    // next packet header - decode
                    if (shiftReg[eLink] == syncHeaderPattern) {
                        // sync header
                        eLinkStats.getSyncCount()[eLink]++;
                        headerBitCount[eLink] = 0;

                        System.out.println("DDD: **************** SYNC HEADER  elink = " + eLink +
                                " shiftReg = " + shiftReg[eLink] +
                                " syncCount = " + eLinkStats.getSyncCount()[eLink]);
                    } else {
                        // non-sync packet header - identify type
                        pkt = (int) ((shiftReg[eLink] >>> 7) & 0x7);
                        numWords[eLink] = (int) ((shiftReg[eLink] >>> 10) & 0x3FF);
                        hadd = (int) ((shiftReg[eLink] >>> 20) & 0xF);
                        chadd = (int) ((shiftReg[eLink] >>> 24) & 0x1F);
                        bxCount = (int) ((shiftReg[eLink] >>> 29) & 0xFFFFF);
                        hamming = (int) ((shiftReg[eLink]) & 0x3F);
                        dataParity = (int) ((shiftReg[eLink] >>> 49) & 0x1);
                        parity = (int) ((shiftReg[eLink] >>> 6) & 0x1);

                        if ((pkt == 0) && (numWords[eLink] == 0) && (chadd == 0x15)) {
                            // heartbeat packet (NO payload) - push into output stream
                            eLinkStats.getHeartBeatCount()[eLink]++;
                            headerBitCount[eLink] = 0;
                            head1 = 0xA0000000 | (bxCount << 9) | (chadd << 4) | hadd;
                            head2 = 0x40000000 | (parity << 23) | (hamming << 17) | (dataParity << 16) | (numWords[eLink] << 3) | pkt;

                            System.out.println("DDD: **************** HEARTBEAT HEADER  elink = " + eLink
                                    + " shiftReg = " + shiftReg[eLink] +
                                    " heartBeatCount = " + eLinkStats.getHeartBeatCount()[eLink]);
                        } else if (pkt == 4) {
                            // initially require only NORMAL data packet headers
                            // check consistency of data header - verify that 'hadd' and chadd' are consistent with 'eLink' number
                            match = matchDataHeader(eLink, hadd, chadd);

                            if (match) {
                                // header consistent with data header
                                dataCount[eLink] = dataCount[eLink] + 1;
                                eLinkStats.getDataHeaderCount()[eLink]++;
                                fec_channel = (hadd * 32) + chadd;
                                if ((fec_channel >= 0) && (fec_channel <= 159)) {
                                    eLinkStats.getDataChannelCount()[fec_channel]++;
                                } else {
                                    System.out.println("DDD:  -------- ILLEGAL CHANNEL NUMBER  elink = " + eLink +
                                            " hadd = " + hadd + " chadd = " + chadd);
                                }
                                dataHeader[eLink] = 1;
                                dataBitCount[eLink] = 0;
                                dataWordCount[eLink] = 0;
                                headerBitCount[eLink] = 0;
                                head1 = 0xA0000000 | (bxCount << 9) | (chadd << 4) | hadd;
                                head2 = 0x40000000 | (parity << 23) | (hamming << 17) | (dataParity << 16) | (numWords[eLink] << 3) | pkt;

                                eLinkDataTemp[eLink].add(head1);        // push header into temporary storage vector
                                eLinkDataTemp[eLink].add(head2);

                                System.out.println("DDD: **************** DATA HEADER  elink = " + eLink +
                                        " shiftReg = " + shiftReg[eLink] +
                                        " pkt = " + pkt +
                                        " dataCount = " + dataCount[eLink] +
                                        " numWords = " + numWords[eLink] +
                                        " hadd = " + hadd +
                                        " chadd = " + chadd +
                                        " bxCount = " + bxCount);
                            } else {
                                // inconsistent data header - force the finding of next sync header
                                headerBitCount[eLink] = 0;
                                syncFound[eLink] = 0;
                                eLinkStats.getSyncLostCount()[eLink]++;
                                System.out.println("DDD: UNRECOGNIZED HEADER  elink = " + eLink +
                                        " shiftReg = " + shiftReg[eLink] +
                                        " pkt = ");
                            }
                        } else {
                            // 'unrecognized' header - force the finding of next sync header
                            headerBitCount[eLink] = 0;
                            syncFound[eLink] = 0;
                            eLinkStats.getSyncLostCount()[eLink]++;
                            System.out.println("DDD: -------- UNRECOGNIZED HEADER  elink = " + eLink +
                                    " shiftReg = " + shiftReg[eLink] +
                                    " pkt = " + pkt);
                        }
                    }
                }
                //-----------------------------------------------------------------------
            }
        } else if (dataHeader[eLink] > 0) {
            // runs only after data packet header has been found
            for (int ii = ii_max; ii >= ii_min; ii--) {
                // elink (4 bits per frame)
                bitValue = (gFrameWord & (0x00000001 << ii)) >>> ii;
                if (bitValue > 0)
                    shiftReg[eLink] = shiftReg[eLink] | 0x0004000000000000L;        // set bit 50 in shiftReg
                shiftReg[eLink] = shiftReg[eLink] >>> 1;

                if (dataHeader[eLink] > 0)                // count data word bits until data payload is exhausted
                    dataBitCount[eLink] = dataBitCount[eLink] + 1;
                else                            // if payload is exhausted count remaining bits of frame for next header
                    headerBitCount[eLink] = headerBitCount[eLink] + 1;

                if (dataBitCount[eLink] == 10) {
                    // print data word
                    dataWordCount[eLink] = dataWordCount[eLink] + 1;
                    dataWord = (int) ((shiftReg[eLink] >>> 40) & 0x3FF);
                    dataValue = (dataWordCount[eLink] << 16) | dataWord;
                    eLinkDataTemp[eLink].add(dataValue);        // push data into temporary storage vector

                    System.out.println("DDD:  shiftReg = " + shiftReg[eLink] +
                            " data(hex) = " + Integer.toHexString( dataWord) +
                            " data = " + dataWord +
                            " dataWordCount = " + dataWordCount[eLink] +
                            " elink = " + eLink);
                    dataBitCount[eLink] = 0;
                }

                if (dataWordCount[eLink] == numWords[eLink]) {
                    // done with packet payload
                    // Both header words and all packet data words have been stored in a temporary vector.
                    // This is done to assure that only complete packets appear in the output data stream.
                    // Now copy the temporary vector to the output stream.
                    for (int jj = 0; jj < (numWords[eLink] + 2); jj++) {
                        tempData = eLinkDataTemp[eLink].get(jj);
                        eLinkData[eLink].add(tempData);        // copy temp data into output stream vector
                    }
                    eLinkDataTemp[eLink].clear();                // delete all data of temporary vector

                    dataHeader[eLink] = 0;                    // reset
                    headerBitCount[eLink] = 0;
                    dataBitCount[eLink] = 0;
                    dataWordCount[eLink] = 0;
                    System.out.println("DDD: END OF DATA  elink = " + eLink);
                }
            }
        }
    }

    public static boolean matchDataHeader(int eLink, int hadd, int chadd) {
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
        if (eLink < 21) {
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

    public void printLinkStats(){
        // print elink stats
        int channel;

        System.out.println();

        for(int ii = 0; ii < 28; ii++)
        {
            System.out.println( "-------------------------------- elink = " + ii +  " ---------------------------------------- \n");
            System.out.println( " sync count = " + eLinkStats.getSyncCount()[ii]
                    + "  sync found count = " + eLinkStats.getSyncFoundCount()[ii]
                    + "  sync lost count = " + eLinkStats.getSyncLostCount()[ii] + "\n");
            System.out.println( " data header count = " + eLinkStats.getDataHeaderCount()[ii]
                    + "  heartbeat count = " + eLinkStats.getHeartBeatCount()[ii] + "\n");
        }

        System.out.println("\n --------------------------------------------- channel counts -----------------------------------------------");

        for(int chip = 0; chip < 5;chip++)
        {
            for(int ch = 0; ch < 32; ch++)
            {
                channel = chip*32 + ch;
                if( (channel%16) == 0 )
                    System.out.print("\n" + "chan " + channel +": ");
                if( (channel%16) == 8 )
                    System.out.print("  ");
                System.out.print( eLinkStats.getDataChannelCount()[channel]+" ");
                if( channel == 79 )
                    System.out.println();
            }
        }

        System.out.println("\n------------------------------------------------------------------------------------------------------------\n\n");

    }

    private void reset() {
        eLinkStats.reset();
        Arrays.fill(shiftReg, 0);
        Arrays.fill(syncFound, 0);
        Arrays.fill(dataHeader, 0);
        Arrays.fill(headerBitCount, 0);
        Arrays.fill(dataBitCount, 0);
        Arrays.fill(dataWordCount, 0);
        Arrays.fill(dataCount, 0);
        Arrays.fill(numWords, 0);
        Arrays.stream(eLinkDataTemp).forEach(Vector::clear);
        Arrays.stream(eLinkData).forEach(Vector::clear);
    }
}
