/*
 * Copyright (c) 2021, Jefferson Science Associates, all rights reserved.
 * See LICENSE.txt file.
 *
 * Thomas Jefferson National Accelerator Facility
 * Experimental Physics Software and Computing Infrastructure Group
 *
 * 12000, Jefferson Ave, Newport News, VA 23606
 * Phone : (757)-269-7100
 */

package org.jlab.epsci.stream.sampaBB;


/**
 * Class to work with ReceiveAndDumper. It starts 2 of those objects which acts as
 * replacements for SReceivers. They read, then dump data in a cycle for a max of the
 * given number of frames.
 * Useful only for testing the data flow from the board to the first receiving component.
 * @author timmer
 */
public class DoubleDumper {

    /**
     * SAMPA ports and stream info
     */
    private final int sampaPort1;
    private final int sampaPort2;
    private final int streamId1;
    private final int streamId2;
    private final int streamFrameLimit;

    /** Format of data SAMPA chips are sending. */
    private final SampaType sampaType;

    public DoubleDumper(int sampaPort1, int sampaPort2,
                        int streamId1, int streamId2,
                        int streamFrameLimit,
                        SampaType sampaType) {

        this.sampaPort1 = sampaPort1;
        this.sampaPort2 = sampaPort2;
        this.streamId1 = streamId1;
        this.streamId2 = streamId2;
        this.streamFrameLimit = streamFrameLimit;
        this.sampaType = sampaType;
    }

    public void go() {
        ReceiveAndDumper receiver1 = new ReceiveAndDumper(sampaPort1, streamId1, streamFrameLimit, sampaType);
        ReceiveAndDumper receiver2 = new ReceiveAndDumper(sampaPort2, streamId2, streamFrameLimit, sampaType);

        receiver1.start();
        receiver2.start();
    }

    /**
     * Main method. Arguments are:
     * <ol>
     * <li>port of TCP server to run in first ReceiveAndDumper object
     * <li>port of TCP server to run in second ReceiveAndDumper object
     * <li>id of first ReceiveAndDumper's data stream
     * <li>id of second ReceiveAndDumper's data stream
     * <li>limit on number of frames to parse on each stream
     * <li>optional: if = DAS, it switches from parsing DSP format to DAS format data
     * </ol>
     *
     * Really only designed for DAS mode.
     *
     * @param args array of args.
     */
    public static void main(String[] args) {
        int port1 = Integer.parseInt(args[0]);
        int port2 = Integer.parseInt(args[1]);

        int streamId1 = Integer.parseInt(args[2]);
        int streamId2 = Integer.parseInt(args[3]);

        int streamFrameLimit = Integer.parseInt(args[4]);

        SampaType sampaType = SampaType.DSP;
        if (args.length > 5) {
            String sType = args[5];
            if (sType.equalsIgnoreCase("das")) {
                sampaType = SampaType.DAS;
            }
        }

        try {
            new DoubleDumper(port1, port2, streamId1, streamId2,
                             streamFrameLimit, sampaType).go();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
