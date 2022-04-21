package org.jlab.epsci.stream.vtp.evio;

import com.lmax.disruptor.RingBuffer;
import org.jlab.coda.jevio.*;
import org.jlab.epsci.ersap.util.report.JsonUtils;
import org.jlab.epsci.stream.util.EUtil;
import org.jlab.epsci.stream.vtp.VRingRawEvent;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Copyright (c) 2021, Jefferson Science Associates, all rights reserved.
 * See LICENSE.txt file.
 * Thomas Jefferson National Accelerator Facility
 * Experimental Physics Software and Computing Infrastructure Group
 * 12000, Jefferson Ave, Newport News, VA 23606
 * Phone : (757)-269-7100
 *
 * @author gurjyan on 4/19/22
 * @project ersap-vtp
 */
public class VtpFileReceiver extends Thread {

     // VTP data stream
    private DataInputStream dataInputStream;

     // Stream ID
    private int streamId;

    // EVIO file name
    private String fName;

     // Output ring
    private RingBuffer<VRingRawEvent> ringBuffer;

     // Current spot in the ring from which an item was claimed.
    private long sequenceNumber;


    // control for the thread termination
    private AtomicBoolean running = new AtomicBoolean(true);

    private ByteBuffer vtpHeaderBuffer;
    private byte[] vtpHeader = new byte[52];

    private ByteBuffer fileBuffer;
    private byte[] fileBytes;

    public VtpFileReceiver(String fName, int streamId, RingBuffer<VRingRawEvent> ringBuffer) {
        this.fName = fName;
        this.ringBuffer = ringBuffer;
        this.streamId = streamId;

        vtpHeaderBuffer = ByteBuffer.wrap(vtpHeader);
        vtpHeaderBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Get the next available item in ring buffer for writing data.
     *
     * @return next available item in ring buffer.
     * @throws InterruptedException if thread interrupted.
     */
    private VRingRawEvent get() throws InterruptedException {

        sequenceNumber = ringBuffer.next();
        VRingRawEvent buf = ringBuffer.get(sequenceNumber);
        return buf;
    }

    private void publish() {
        ringBuffer.publish(sequenceNumber);
    }


    private void decodeVtpHeader(VRingRawEvent evt) {
        try {
            vtpHeaderBuffer.clear();
            dataInputStream.readFully(vtpHeader);

            int source_id = vtpHeaderBuffer.getInt();
            int total_length = vtpHeaderBuffer.getInt();
            int payload_length = vtpHeaderBuffer.getInt();
            int compressed_length = vtpHeaderBuffer.getInt();
            int magic = vtpHeaderBuffer.getInt();

            int format_version = vtpHeaderBuffer.getInt();
            int flags = vtpHeaderBuffer.getInt();

            long record_number = EUtil.llSwap(vtpHeaderBuffer.getLong());
            long ts_sec = EUtil.llSwap(vtpHeaderBuffer.getLong());
            long ts_nsec = EUtil.llSwap(vtpHeaderBuffer.getLong());

//            long record_number = headerBuffer.getLong();
//            long ts_sec = headerBuffer.getLong();
//            long ts_nsec = headerBuffer.getLong();

//            printFrame(streamId, source_id, total_length, payload_length,
//                    compressed_length, magic, format_version, flags,
//                    record_number, ts_sec, ts_nsec);

            if (evt.getPayload().length < payload_length) {
                byte[] payloadData = new byte[payload_length];
                evt.setPayload(payloadData);
            }
            dataInputStream.readFully(evt.getPayload(), 0, payload_length);

//            evt.setRecordNumber(rcn);
            evt.setPayloadDataLength(payload_length);
            evt.setRecordNumber(record_number);
            evt.setStreamId(streamId);

            // Collect statistics
//            long tmp = missed_record + (record_number - (prev_rec_number + 1));
//            missed_record = tmp;
//
//            prev_rec_number = record_number;
//            totalData = totalData + (double) total_length / 1000.0;
//            rate++;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            EvioReader evioReader;
            EvioEvent event;
            System.out.println("read evio file: " + fName);
            evioReader = new EvioReader(fName);

            EventParser parser = evioReader.getParser();

            IEvioListener listener = new IEvioListener() {
                public void gotStructure(BaseStructure topStructure, IEvioStructure structure) {
                    System.out.println("Parsed structure of type " + structure.getStructureType());
                }

                public void startEventParse(BaseStructure structure) {
                    System.out.println("Starting event parse");
                }

                public void endEventParse(BaseStructure structure) {
                    System.out.println("Ended event parse");
                }
            };

            parser.addEvioListener(listener);

            // Get any existing dictionary (should be the same as "xmlDictionary")
            String xmlDictString = evioReader.getDictionaryXML();
            EvioXMLDictionary dictionary = null;

            if (xmlDictString == null) {
                System.out.println("No dictionary found.");
            } else {
                // Create dictionary object from xml string
                dictionary = new EvioXMLDictionary(xmlDictString);
                System.out.println("Got a dictionary:\n" + dictionary);
            }

            // How many events in the file?
            int evCount = evioReader.getEventCount();
            System.out.println("Read file, got " + evCount + " events:\n");

            // Use sequential access to events
            while ((event = evioReader.parseNextEvent()) != null) {
                event.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                System.out.println("Event = " + event);
                int tag = event.getHeader().getTag();
                System.out.println("tag = " + tag);
                if (tag != 0xFF60) continue;

                List<BaseStructure> l = event.getChildrenList();
                for (BaseStructure b : l) {
                    if (b.getHeader().getTag() == 0xFF31) {
                        int[] timeBank = b.getIntData();
                        System.out.println("timeBank length = " + timeBank[0]);
                        System.out.println("frame number = " + timeBank[2]);
                        long timeStamp = ((long) timeBank[3] << 32) & timeBank[4];
                        System.out.println(timeStamp);
                    } else {
                        try {
                            // Get an empty item from ring
                            VRingRawEvent buf = get();

                            decodeVtpHeader(buf);

                            // Make the buffer available for consumers
                            publish();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }


        } catch (EvioException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

        public void exit() {
        running.set(false);
        try {
            dataInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.interrupt();
    }

}
