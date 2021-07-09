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
import org.jlab.epsci.stream.sampaBB.DecoderType;
import org.jlab.epsci.stream.sampaBB.SRingRawEvent;
import org.jlab.epsci.stream.sampaBB.SampaDecoder;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;


/**
 * Direct ADC Serialization (DAS) mode of operating the SAMPA board.
 * This is one of 2 standard modes of readout, the other being the DSP mode.
 */
@SuppressWarnings("unchecked")
public class SDasDecoder implements SampaDecoder {

    private String input_file_name_;
    private int file_size_;

    private boolean verbosity_;
    private int num_timebins_;


    // SYNC positions
    private final int sync_unknown_ = -1;

    private int sync_low_  = sync_unknown_;
    private int sync_high_ = sync_unknown_;
    private int sync_low_1_;
    private int sync_high_1_;
    private int sync_2_;

    private int channel_offset_low_ = 0;
    private int channel_offset_high_ = 16;
    private int channel_offset_low_1_ = 32;
    private int channel_offset_high_1_ = 48;
    private int channel_offset_2_ = 64;

    // vector for SAMPA half-words (5-bit)
    private ArrayList<Integer> sampa_stream_low_    = new ArrayList<>(); // for SAMPA0 (or SAMPA 3)
    private ArrayList<Integer> sampa_stream_high_   = new ArrayList<>();
    private ArrayList<Integer> sampa_stream_low_1_  = new ArrayList<>(); // for SAMPA1 (or SAMPA 4)
    private ArrayList<Integer> sampa_stream_high_1_ = new ArrayList<>();
    private ArrayList<Integer> sampa_stream_2_      = new ArrayList<>(); // for SAMPA2 (1 stream only)

    private ArrayList<Integer> sampa_stream_clock_0_ = new ArrayList<>(); // for SAMPA0 e-link 10 (clock)
    private ArrayList<Integer> sampa_stream_clock_1_ = new ArrayList<>(); // for SAMPA1 e-link 10 (clock)
    private ArrayList<Integer> sampa_stream_clock_2_ = new ArrayList<>(); // for SAMPA3 e-link 10 (clock)

    private ArrayList<Integer>[] sampaData = new ArrayList[80];	// for two and a half SAMPAs in the data stream

    // statistics
    private double[] mean = new double[80];
    private double[] sdv  = new double[80];

    // SYNC Pattern. The following sequence in the data is the SYNC pattern. 0 = 0x2B5 and 1 = 0x14A
    private static final int[] SYNC_PATTERN = { 0x015, 0x015, 0x00a, 0x00a, 0x015, 0x015, 0x00a, 0x00a, 0x015, 0x015, 0x00a,
            0x00a, 0x015, 0x015, 0x00a, 0x00a, 0x015, 0x015, 0x015, 0x015, 0x00a, 0x00a,
            0x00a, 0x00a, 0x015, 0x015, 0x015, 0x015, 0x00A, 0x00A, 0x00A, 0x00A };




    public SDasDecoder(int num_timebins, boolean verbosity) {
        verbosity_    = verbosity;
        num_timebins_ = num_timebins;

        for (int i=0 ; i < sampaData.length; i++) {
            sampaData[i] = new ArrayList<>();
        }
    }

    public DecoderType getDecoderType() {return DecoderType.DAS;}

    public int getBlockCount() {int block_count = 0; return block_count;}
    public boolean isBlockComplete() {
        //return block_frameCount == frames_in_block;
        return true;
    }

    public void printBlockData(int streamId, org.jlab.epsci.stream.sampaBB.SRingRawEvent rawEvent) {
        // do nothing
    }

    public void decodeSerial(int eLink, int[] gbt_frame, SRingRawEvent rawEvent) throws Exception {
        getHalfWords(gbt_frame);
    }

    public void printLinkStats() {
        // do nothing
    }

    //private int bit(int d, int src, int trg) { return ((d & (1 << src)) >> src) << trg; }
    private int bit(int d, int src, int trg) { return ((d >>> src) & 1) << trg; }


    private void getHalfWords(int[] gf) {

        // extract the 4 halfwords for SAMPA0 higher data stream and insert them into the stream_high vector
        sampa_stream_high_.add(bit(gf[1], 7, 4) | bit(gf[1], 3, 3) | bit(gf[0], 31, 2) | bit(gf[0], 27, 1) | bit(gf[0], 23, 0));
        sampa_stream_high_.add(bit(gf[1], 6, 4) | bit(gf[1], 2, 3) | bit(gf[0], 30, 2) | bit(gf[0], 26, 1) | bit(gf[0], 22, 0));
        sampa_stream_high_.add(bit(gf[1], 5, 4) | bit(gf[1], 1, 3) | bit(gf[0], 29, 2) | bit(gf[0], 25, 1) | bit(gf[0], 21, 0));
        sampa_stream_high_.add(bit(gf[1], 4, 4) | bit(gf[1], 0, 3) | bit(gf[0], 28, 2) | bit(gf[0], 24, 1) | bit(gf[0], 20, 0));

        // extract the 4 halfwords for SAMPA0 lower data stream and insert them into the stream_low vector
        sampa_stream_low_.add(bit(gf[0], 19, 4) | bit(gf[0], 15, 3) | bit(gf[0], 11, 2) | bit(gf[0], 7, 1) | bit(gf[0], 3, 0));
        sampa_stream_low_.add(bit(gf[0], 18, 4) | bit(gf[0], 14, 3) | bit(gf[0], 10, 2) | bit(gf[0], 6, 1) | bit(gf[0], 2, 0));
        sampa_stream_low_.add(bit(gf[0], 17, 4) | bit(gf[0], 13, 3) | bit(gf[0],  9, 2) | bit(gf[0], 5, 1) | bit(gf[0], 1, 0));
        sampa_stream_low_.add(bit(gf[0], 16, 4) | bit(gf[0], 12, 3) | bit(gf[0],  8, 2) | bit(gf[0], 4, 1) | bit(gf[0], 0, 0));

        // extract the 4 halfwords for SAMPA1 higher data stream and insert them into the stream_high vector
        sampa_stream_high_1_.add(bit(gf[2], 19, 4) | bit(gf[2], 15, 3) | bit(gf[2], 11, 2) | bit(gf[2], 7, 1) | bit(gf[2], 3, 0));
        sampa_stream_high_1_.add(bit(gf[2], 18, 4) | bit(gf[2], 14, 3) | bit(gf[2], 10, 2) | bit(gf[2], 6, 1) | bit(gf[2], 2, 0));
        sampa_stream_high_1_.add(bit(gf[2], 17, 4) | bit(gf[2], 13, 3) | bit(gf[2], 9, 2)  | bit(gf[2], 5, 1) | bit(gf[2], 1, 0));
        sampa_stream_high_1_.add(bit(gf[2], 16, 4) | bit(gf[2], 12, 3) | bit(gf[2], 8, 2)  | bit(gf[2], 4, 1) | bit(gf[2], 0, 0));

        // extract the 4 halfwords for SAMPA1 lower data stream and insert them into the stream_low vector
        sampa_stream_low_1_.add(bit(gf[1], 31, 4) | bit(gf[1], 27, 3) | bit(gf[1], 23, 2) | bit(gf[1], 19, 1) | bit(gf[1], 15, 0));
        sampa_stream_low_1_.add(bit(gf[1], 30, 4) | bit(gf[1], 26, 3) | bit(gf[1], 22, 2) | bit(gf[1], 18, 1) | bit(gf[1], 14, 0));
        sampa_stream_low_1_.add(bit(gf[1], 29, 4) | bit(gf[1], 25, 3) | bit(gf[1], 21, 2) | bit(gf[1], 17, 1) | bit(gf[1], 13, 0));
        sampa_stream_low_1_.add(bit(gf[1], 28, 4) | bit(gf[1], 24, 3) | bit(gf[1], 20, 2) | bit(gf[1], 16, 1) | bit(gf[1], 12, 0));

        // extract the 4 halfwords for SAMPA2 data stream and insert them into the stream_vector
        sampa_stream_2_.add(bit(gf[3], 11, 4) | bit(gf[3], 7, 3) | bit(gf[3], 3, 2) | bit(gf[2], 31, 1) | bit(gf[2], 27, 0));
        sampa_stream_2_.add(bit(gf[3], 10, 4) | bit(gf[3], 6, 3) | bit(gf[3], 2, 2) | bit(gf[2], 30, 1) | bit(gf[2], 26, 0));
        sampa_stream_2_.add(bit(gf[3],  9, 4) | bit(gf[3], 5, 3) | bit(gf[3], 1, 2) | bit(gf[2], 29, 1) | bit(gf[2], 25, 0));
        sampa_stream_2_.add(bit(gf[3],  8, 4) | bit(gf[3], 4, 3) | bit(gf[3], 0, 2) | bit(gf[2], 28, 1) | bit(gf[2], 24, 0));

        // SAMPA e-link 10 (clock) streams
        sampa_stream_clock_0_.add(bit(gf[1], 11, 0));
        sampa_stream_clock_0_.add(bit(gf[1], 10, 0));
        sampa_stream_clock_0_.add(bit(gf[1],  9, 0));
        sampa_stream_clock_0_.add(bit(gf[1],  8, 0));

        sampa_stream_clock_1_.add(bit(gf[2], 23, 0));
        sampa_stream_clock_1_.add(bit(gf[2], 22, 0));
        sampa_stream_clock_1_.add(bit(gf[2], 21, 0));
        sampa_stream_clock_1_.add(bit(gf[2], 20, 0));

        sampa_stream_clock_2_.add(bit(gf[3], 15, 0));
        sampa_stream_clock_2_.add(bit(gf[3], 14, 0));
        sampa_stream_clock_2_.add(bit(gf[3], 13, 0));
        sampa_stream_clock_2_.add(bit(gf[3], 12, 0));


        // for debug printing of data streams
        if (verbosity_) {

            long lowInt = 0xffffffffL;

            // Get rid of any negative values
            System.out.println(" w3 = " + ((long)gf[3] & lowInt) + " w2 = " + ((long)gf[2] & lowInt) +
                    " w1 = " + ((long)gf[1] & lowInt) + " w0 = " + ((long)gf[0] & lowInt));

            long bw1_7_4  = ((long)bit(gf[1],7,4)  & lowInt);
            long bw1_3_3  = ((long)bit(gf[1],3,3)  & lowInt);
            long bw0_31_2 = ((long)bit(gf[0],31,2) & lowInt);
            long bw0_27_1 = ((long)bit(gf[0],27,1) & lowInt);
            long bw0_23_0 = ((long)bit(gf[0],23,0) & lowInt);
            System.out.println(" bw1_7_4  = " + bw1_7_4  + " bw1_3_3 = "  + bw1_3_3  + " bw0_31_2 = " + bw0_31_2 +
                    " bw0_27_1 = " + bw0_27_1 + " bw0_23_0 = " + bw0_23_0);

            long bw1_6_4  = ((long)bit(gf[1],6,4)  & lowInt);
            long bw1_2_3  = ((long)bit(gf[1],2,3)  & lowInt);
            long bw0_30_2 = ((long)bit(gf[0],30,2) & lowInt);
            long bw0_26_1 = ((long)bit(gf[0],26,1) & lowInt);
            long bw0_22_0 = ((long)bit(gf[0],22,0) & lowInt);
            System.out.println(" bw1_6_4  = " + bw1_6_4  + " bw1_2_3 = "  + bw1_2_3  + " bw0_30_2 = " + bw0_30_2 +
                    " bw0_26_1 = " + bw0_26_1 + " bw0_22_0 = " + bw0_22_0);

            long bw1_5_4  = ((long)bit(gf[1],5,4)  & lowInt);
            long bw1_1_3  = ((long)bit(gf[1],1,3)  & lowInt);
            long bw0_29_2 = ((long)bit(gf[0],29,2) & lowInt);
            long bw0_25_1 = ((long)bit(gf[0],25,1) & lowInt);
            long bw0_21_0 = ((long)bit(gf[0],21,0) & lowInt);
            System.out.println(" bw1_5_4  = " + bw1_5_4  + " bw1_1_3 = "  + bw1_1_3  + " bw0_29_2 = " + bw0_29_2 +
                    " bw0_25_1 = " + bw0_25_1 + " bw0_21_0 = " + bw0_21_0);

            long bw1_4_4  = ((long)bit(gf[1],4,4)  & lowInt);
            long bw1_0_3  = ((long)bit(gf[1],0,3)  & lowInt);
            long bw0_28_2 = ((long)bit(gf[0],28,2) & lowInt);
            long bw0_24_1 = ((long)bit(gf[0],24,1) & lowInt);
            long bw0_20_0 = ((long)bit(gf[0],20,0) & lowInt);
            System.out.println(" bw1_4_4  = " + bw1_4_4  + " bw1_0_3 = "  + bw1_0_3  + " bw0_28_2 = " + bw0_28_2 +
                    " bw0_24_1 = " + bw0_24_1 + " bw0_20_0 = " + bw0_20_0);

            long high_1  = bw1_7_4 | bw1_3_3 | bw0_31_2 | bw0_27_1 | bw0_23_0;
            long high_2  = bw1_6_4 | bw1_2_3 | bw0_30_2 | bw0_26_1 | bw0_22_0;
            long high_3  = bw1_5_4 | bw1_1_3 | bw0_29_2 | bw0_25_1 | bw0_21_0;
            long high_4  = bw1_4_4 | bw1_0_3 | bw0_28_2 | bw0_24_1 | bw0_20_0;
            System.out.println(" high_1 = " + high_1  + " high_2 = "  + high_2  + " high_3 = " + high_3 + " high_4 = " + high_4);


            long bw0_19_4 = ((long)bit(gf[0],19,4) & lowInt);
            long bw0_15_3 = ((long)bit(gf[0],15,3) & lowInt);
            long bw0_11_2 = ((long)bit(gf[0],11,2) & lowInt);
            long bw0_7_1  = ((long)bit(gf[0],7,1)  & lowInt);
            long bw0_3_0  = ((long)bit(gf[0],3,0)  & lowInt);
            System.out.println(" bw0_19_4 = " + bw0_19_4 + " bw0_15_3 = " + bw0_15_3 + " bw0_11_2 = " + bw0_11_2 +
                    " bw0_7_1  = " + bw0_7_1  + " bw0_3_0 = "  + bw0_3_0);

            long bw0_18_4 = ((long)bit(gf[0],18,4) & lowInt);
            long bw0_14_3 = ((long)bit(gf[0],14,3) & lowInt);
            long bw0_10_2 = ((long)bit(gf[0],10,2) & lowInt);
            long bw0_6_1  = ((long)bit(gf[0],6,1)  & lowInt);
            long bw0_2_0  = ((long)bit(gf[0],2,0)  & lowInt);
            System.out.println(" bw0_18_4 = " + bw0_18_4 + " bw0_14_3 = " + bw0_14_3 + " bw0_10_2 = " + bw0_10_2 +
                    " bw0_6_1  = " + bw0_6_1  + " bw0_2_0 = "  + bw0_2_0);

            long bw0_17_4 = ((long)bit(gf[0],17,4) & lowInt);
            long bw0_13_3 = ((long)bit(gf[0],13,3) & lowInt);
            long bw0_9_2  = ((long)bit(gf[0],9,2)  & lowInt);
            long bw0_5_1  = ((long)bit(gf[0],5,1)  & lowInt);
            long bw0_1_0  = ((long)bit(gf[0],1,0)  & lowInt);
            System.out.println(" bw0_17_4 = " + bw0_17_4 + " bw0_13_3 = " + bw0_13_3 + " bw0_9_2 = " + bw0_9_2 +
                    " bw0_5_1  = " + bw0_5_1  + " bw0_1_0 = "  + bw0_1_0);

            long bw0_16_4 = ((long)bit(gf[0],16,4) & lowInt);
            long bw0_12_3 = ((long)bit(gf[0],12,3) & lowInt);
            long bw0_8_2  = ((long)bit(gf[0],8,2)  & lowInt);
            long bw0_4_1  = ((long)bit(gf[0],4,1)  & lowInt);
            long bw0_0_0  = ((long)bit(gf[0],0,0)  & lowInt);
            System.out.println(" bw0_16_4 = " + bw0_16_4 + " bw0_12_3 = " + bw0_12_3 + " bw0_8_2 = " + bw0_8_2 +
                    " bw0_4_1  = " + bw0_4_1  + " bw0_0_0 = "  + bw0_0_0);

            long low_1  = bw0_19_4 | bw0_15_3 | bw0_11_2 | bw0_7_1 | bw0_3_0;
            long low_2  = bw0_18_4 | bw0_14_3 | bw0_10_2 | bw0_6_1 | bw0_2_0;
            long low_3  = bw0_17_4 | bw0_13_3 | bw0_9_2  | bw0_5_1 | bw0_1_0;
            long low_4  = bw0_16_4 | bw0_12_3 | bw0_8_2  | bw0_4_1 | bw0_0_0;
            System.out.println(" low_1 = " + low_1  + " low_2 = "  + low_2  + " low_3 = " + low_3 + " low_4 = " + low_4);
        }
    }


    /**
     * Find the sync position for all streams and store internally.
     * @return true if all syncs found, else false.
     */
    public boolean getSync() {
        sync_low_    = findSync(sampa_stream_low_, 0);
        sync_high_   = findSync(sampa_stream_high_, 0);
        sync_low_1_  = findSync(sampa_stream_low_1_, 0);
        sync_high_1_ = findSync(sampa_stream_high_1_, 0);
        sync_2_      = findSync(sampa_stream_2_, 0);

        boolean allSyncsFound = true;

        if ((sync_low_    == sync_unknown_) ||
                (sync_high_   == sync_unknown_) ||
                (sync_low_1_  == sync_unknown_) ||
                (sync_high_1_ == sync_unknown_) ||
                (sync_2_      == sync_unknown_))  {
            allSyncsFound = false;
        }

        if (verbosity_) {
            System.out.println("SYNC SAMPA0 Stream Low  : " + sync_low_);
            System.out.println("SYNC SAMPA0 Stream High : " + sync_high_);
            System.out.println("SYNC SAMPA1 Stream Low  : " + sync_low_1_);
            System.out.println("SYNC SAMPA1 Stream High : " + sync_high_1_);
            System.out.println("SYNC SAMPA2 Stream  : " + sync_2_);
        }

        return allSyncsFound;
    }

//    /**
//     * Find the sync position for all streams and store internally.
//     *
//     * @param throw_if_not_found if true, throw an exception if not all syncs found.
//     * @return true if all syncs found, else false.
//     * @throws Exception if not all syncs found.
//     */
//    public boolean getSync(boolean throw_if_not_found) throws Exception {
//        sync_low_    = findSync(sampa_stream_low_, 0);
//        sync_high_   = findSync(sampa_stream_high_, 0);
//        sync_low_1_  = findSync(sampa_stream_low_1_, 0);
//        sync_high_1_ = findSync(sampa_stream_high_1_, 0);
//        sync_2_      = findSync(sampa_stream_2_, 0);
//
//        boolean allSyncsFound = true;
//
//        if (sync_low_ == sync_unknown_) {
//            allSyncsFound = false;
//            if (throw_if_not_found) throw new Exception("Unable to detect sync position in SAMPA0 low stream");
//        }
//
//        if (sync_high_ == sync_unknown_) {
//            allSyncsFound = false;
//            if (throw_if_not_found) throw new Exception("Unable to detect sync position in SAMPA0 high stream");
//        }
//
//        if (sync_low_1_ == sync_unknown_) {
//            allSyncsFound = false;
//            if (throw_if_not_found) throw new Exception("Unable to detect sync position in SAMPA1 low stream");
//        }
//
//        if (sync_high_1_ == sync_unknown_) {
//            allSyncsFound = false;
//            if (throw_if_not_found) throw new Exception("Unable to detect sync position in SAMPA1 high stream");
//        }
//
//        if (sync_2_ == sync_unknown_) {
//            allSyncsFound = false;
//            if (throw_if_not_found) throw new Exception("Unable to detect sync position in SAMPA2 stream");
//        }
//
//        if (verbosity_) {
//            System.out.println("SYNC SAMPA0 Stream Low  : " + sync_low_);
//            System.out.println("SYNC SAMPA0 Stream High : " + sync_high_);
//            System.out.println("SYNC SAMPA1 Stream Low  : " + sync_low_1_);
//            System.out.println("SYNC SAMPA1 Stream High : " + sync_high_1_);
//            System.out.println("SYNC SAMPA2 Stream  : " + sync_2_);
//        }
//        return allSyncsFound;
//    }


    public void getAdcValues() {
        extractAdcValues(sampa_stream_low_, sync_low_ + 1, channel_offset_low_);
        extractAdcValues(sampa_stream_high_, sync_high_ + 1, channel_offset_high_);

        extractAdcValues(sampa_stream_low_1_, sync_low_1_ + 1, channel_offset_low_1_);
        extractAdcValues(sampa_stream_high_1_, sync_high_1_ + 1, channel_offset_high_1_);

        extractAdcValues(sampa_stream_2_, sync_2_ + 1, channel_offset_2_);
    }


    /**
     * Loop over the 5-bit half-word stream and search for the SYNC pattern.
     * and find the position of the last SYNC pattern value (0x0A) in the stream.
     * @param data input data "stream".
     * @param startIndex index of 32-bit word to start the search
     * @return position of the last SYNC pattern value (0x0A) in the stream or -1 if unknown.
     */
    public int findSync(ArrayList<Integer> data, int startIndex) {
        int index = 0;

        for (int i = 0; i < data.size(); i++) {
            //System.out.println("Check : " + index + " 0x" + Integer.toHexString(data.get(i)) + " - " + SYNC_PATTERN[index + startIndex]);
            if (data.get(i) == SYNC_PATTERN[index + startIndex])
                index++;
            else
                index = 0;

            if (index == (32 - startIndex))
                return i;
        }
        return sync_unknown_;
    }

    /**
     * Extract the ADC values from the data stream.
     * @param data data from the stream.
     * @param startPos starting position in data.
     * @param channel_offset starting channel.
     */
    public void extractAdcValues(ArrayList<Integer> data, int startPos, int channel_offset) {
        int adc_value, maxSamples, offset;

        // calculate the number of full Adc channel entries in the stream (16 channels * 2 HWs)
        maxSamples = (data.size() - startPos) / 32;

        if (verbosity_)
            System.out.println("Maximum number of samples : " + maxSamples);

        // loop over the samples
        for (int numSamples = 0; numSamples < maxSamples; numSamples++) {
            // extract the 16 channels
            offset = startPos + numSamples * 32;

            for (int channel = 0; channel < 16; channel++) {
                adc_value = (data.get(offset + channel * 2 + 1) << 5) + data.get(offset + channel * 2);
                sampaData[channel + channel_offset].add(adc_value);
            };
        };
    }


    /** Calculate statistics. */
    public void calcStats() {
        double m = 0;
        double M2 = 0;
        double variance = 0;

        // check if the number of TimeBins doesn't exceed the amount of decoded
        // timebins
        int sampleLimit = num_timebins_;
        if (num_timebins_ > sampaData[0].size() || num_timebins_ == 0) {
            sampleLimit = sampaData[0].size();
        }

        for (int channel = 0; channel < 80; channel++) {
            m = 0;
            M2 = 0;
            variance = 0;
            for (int sample = 0; sample < sampleLimit; sample++) {
                double delta = sampaData[channel].get(sample) - m;
                m += delta / (sample + 1);
                M2 += delta * (sampaData[channel].get(sample) - m);
                variance = M2 / (sample + 1);
            };
            mean[channel] = m;
            sdv[channel] = Math.sqrt(variance);

            if (verbosity_)
                System.out.printf("[%02d] : %8.4f   %8.4f",   channel, mean[channel], sdv[channel]);
        };
    }


    /**
     * Write statistics to output stream.
     * @param out output stream
     * @param json true if json format desired
     */
    public void writeStats(OutputStream out, boolean json) {

        boolean autoFlush = true;
        PrintWriter writer = new PrintWriter(out, autoFlush, Charset.forName("US_ASCII"));

        writer.print(5);
        writer.write((json ? "{\n" : ""));
        writer.write((json ? "\"input_file_name\" : " : "File name : "));
        writer.write("\"");
        writer.write(input_file_name_);
        writer.write("\"");
        writer.write((json ? ",\n" : "\n"));

        writer.write((json ? "\"input_file_size\" : " : "File size : ") + file_size_ + (json ? ",\n" : "\n"));
        writer.write((json ? "\"sync_low\"  : " : "SYNC Low : ")  + sync_low_  + (json ? ",\n" : "\n"));
        writer.write((json ? "\"sync_high\" : " : "SYNC High : ") + sync_high_ + (json ? ",\n" : "\n"));

        if (json) {
            writer.write("\"mean\": [");
            for (int channel = 0; channel < 80; channel++) {
                writer.printf("%8.4f", mean[channel]);
                writer.write((channel == 79 ? "]" : ", "));
            }

            writer.write(",\n");
            writer.write("\"stdev\": [");

            for (int channel = 0; channel < 80; channel++) {
                writer.printf("%6.4f", sdv[channel]);
                writer.write((channel == 79 ? "]" : ", "));
            }
        }
        else {
            for (int channel = 0; channel < 80; channel++) {
                writer.write("[ CHA ");
                writer.printf("%2d] : ", channel);
                writer.printf("%8.4f   ", mean[channel]);
                writer.printf("%6.4f\n", sdv[channel]);

            };
        };

        writer.write((json ? "\n}" : ""));
        writer.write("\n");
    }


    /**
     * Write ADC values to output stream.
     * @param out output stream
     */
    public void writeAdcValues(OutputStream out) {
        boolean autoFlush = true;
        PrintWriter writer = new PrintWriter(out, autoFlush, Charset.forName("US_ASCII"));

        // check if the number of TimeBins doesn't exceed the amount of decoded
        // timebins
        int sampleLimit = num_timebins_;
        if (num_timebins_ > sampaData[0].size() || num_timebins_ == 0) {
            sampleLimit = sampaData[0].size();
        }

        for (int sample = 0; sample < sampleLimit; sample++) {
            for (int channel = 0; channel < 80; channel++) {
                writer.printf("%4d", sampaData[channel].get(sample));
                writer.print(" ");
            }
            writer.print("\n");
        }
    }


    public static void main(String[] args) {

        // Instantiate the SDasDecoder
        int samples = 1024;
        org.jlab.epsci.stream.sampaBB.DasDecoder sDec = new org.jlab.epsci.stream.sampaBB.DasDecoder(samples, false);

        try {
            sDec.decodeSerial(1, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // check for the SYNC pattern
        //boolean foundSync = sDec.getSync();

        // extract the Adc values
        sDec.getAdcValues();

        // write Adc values to file
        sDec.writeAdcValues(System.out);

        // calculate the mean and sdv for each Adc channel
        sDec.calcStats();
        sDec.writeStats(System.out,true);

        boolean json = true;
        if (json)
            sDec.writeStats(System.out, true);
        else {
            System.out.println("-----------------------------------");
            System.out.println("          Stats");
            System.out.println("-----------------------------------");
            sDec.writeStats(System.out, false);
        }
    }

}
