package org.jlab.epsci.ersap.util;


import org.jlab.epsci.ersap.util.hitfinder.AdcHit;
import org.jlab.epsci.ersap.util.hitfinder.ChargeTime;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.List;

/**
 * ERSAP utility class
 *
 * @author gurjyan
 * @since 04.01.20
 */
public class EUtil {

    private static final ByteBuffer bb32 = ByteBuffer.allocate(4);
    private static final ByteBuffer bb64 = ByteBuffer.allocate(8);

    private static byte[] i32 = bb32.array();
    private static byte[] i64 = bb64.array();

    /**
     * Returns unsigned byte for a ByteBuffer
     *
     * @param bb input ByteBuffer
     * @return unsigned byte as a short
     */
    public static short getUnsignedByte(ByteBuffer bb) {
        return ((short) (bb.get() & 0xff));
    }

    public static void putUnsignedByte(ByteBuffer bb, int value) {
        bb.put((byte) (value & 0xff));
    }

    public static short getUnsignedByte(ByteBuffer bb, int position) {
        return ((short) (bb.get(position) & (short) 0xff));
    }

    public static void putUnsignedByte(ByteBuffer bb, int position, int value) {
        bb.put(position, (byte) (value & 0xff));
    }

    public static int getUnsignedShort(ByteBuffer bb) {
        return (bb.getShort() & 0xffff);
    }

    public static void putUnsignedShort(ByteBuffer bb, int value) {
        bb.putShort((short) (value & 0xffff));
    }

    public static int getUnsignedShort(ByteBuffer bb, int position) {
        return (bb.getShort(position) & 0xffff);
    }

    public static void putUnsignedShort(ByteBuffer bb, int position, int value) {
        bb.putShort(position, (short) (value & 0xffff));
    }


    public static long getUnsignedInt(ByteBuffer bb) {
        return ((long) bb.getInt() & 0xffffffffL);
    }

    public static void putUnsignedInt(ByteBuffer bb, long value) {
        bb.putInt((int) (value & 0xffffffffL));
    }

    public static long getUnsignedInt(ByteBuffer bb, int position) {
        return ((long) bb.getInt(position) & 0xffffffffL);
    }

    public static void putUnsignedInt(ByteBuffer bb, int position, long value) {
        bb.putInt(position, (int) (value & 0xffffffffL));
    }

    public static long readLteUnsigned32(DataInputStream dataInputStream) {
        try {
            // I made the ByteBuffer object static, Carl T.
            dataInputStream.readFully(i32);
            bb32.order(ByteOrder.LITTLE_ENDIAN);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getUnsignedInt(bb32);
    }

    public static int readUnsigned32(DataInputStream dataInputStream) throws IOException {
        int ch1 = dataInputStream.read();
        int ch2 = dataInputStream.read();
        int ch3 = dataInputStream.read();
        int ch4 = dataInputStream.read();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }


    public static BigInteger toUnsignedBigInteger(long i) {
        if (i >= 0L)
            return BigInteger.valueOf(i);
        else {
            int upper = (int) (i >>> 32);
            int lower = (int) i;

            return (BigInteger.valueOf(Integer.toUnsignedLong(upper))).shiftLeft(32).
                    add(BigInteger.valueOf(Integer.toUnsignedLong(lower)));
        }
    }

    public static BigInteger readLteUnsigned64(DataInputStream dataInputStream) {
        ByteBuffer bb = null;
        try {
            dataInputStream.readFully(i64);
            bb = ByteBuffer.wrap(i64);
            bb.order(ByteOrder.LITTLE_ENDIAN);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert bb != null;
        return toUnsignedBigInteger(bb.getLong());
    }

    public static BigInteger readLteUnsignedSwap64(DataInputStream dataInputStream) {
        ByteBuffer bb = null;
        try {
            dataInputStream.readFully(i64);
            bb = ByteBuffer.wrap(i64);
            bb.order(ByteOrder.LITTLE_ENDIAN);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert bb != null;
        return toUnsignedBigInteger(llSwap(bb.getLong()));
    }


    /**
     * Avoid the creation of long[] objects by providing as an arg.
     *
     * @param dataInputStream
     * @param payload         array to be filled
     */
    public static void readLtPayload(DataInputStream dataInputStream, long[] payload) {
        int j = 0;
        for (long i = 0; i < payload.length; i = i + 4) {
            payload[j] = readLteUnsigned32(dataInputStream);
            j = j + 1;
        }
    }

    public static long[] readLtPayload(DataInputStream dataInputStream, long payload_length) {
        long[] payload = new long[(int) payload_length / 4];
        int j = 0;
        for (long i = 0; i < payload_length; i = i + 4) {
            payload[j] = readLteUnsigned32(dataInputStream);
            j = j + 1;
        }
        return payload;
    }

    public static long llSwap(long l) {
        // ERROR corrected here, Carl T.
        long x = l >>> 32;
        x = x | l << 32;
        return x;
    }

    public static byte[] long2ByteArray(long lng) {
        byte[] b = new byte[]{
                (byte) lng,
                (byte) (lng >> 8),
                (byte) (lng >> 16),
                (byte) (lng >> 24),
                (byte) (lng >> 32),
                (byte) (lng >> 40),
                (byte) (lng >> 48),
                (byte) (lng >> 56)};
        return b;
    }

    public static void busyWaitMicros(long delay) {
        long start = System.nanoTime();
        while (System.nanoTime() - start < delay) ;
    }

    public static <T> T requireNonNull(T obj, String desc) {
        return Objects.requireNonNull(obj, "null " + desc);
    }


    public static void addByteArrays(byte[] a, int aLength, byte[] b, int bLength, byte[] c) {
        System.arraycopy(a, 0, c, 0, aLength);
        System.arraycopy(b, 0, c, aLength, bLength);
    }

    public static void addIntArrays(int[] a, int aLength, int[] b, int bLength, int[] c) {
        System.arraycopy(a, 0, c, 0, aLength);
        System.arraycopy(b, 0, c, aLength, bLength);
    }

    public static byte[] object2ByteArray(Object o) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.flush();
        return bos.toByteArray();
    }

    public static Object byteArray2Object(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    public static int encodeCSC(int crate, int slot, int channel) {
        return (crate << 16) | (slot << 8) | (channel << 4);
    }

    public static int decodeCrateNumber(int csc) {
        return csc >>> 16;
    }

    public static int decodeSlotNumber(int csc) {
        // TODO: This seemed like a bug to me so I fixed it, Carl T.
        //return csc & 0x000000f0;
        return (csc >> 8) & 0xf;
    }

    public static int decodeChannelNumber(int csc) {
        return csc & 0x0000000f;
    }

    public static Map<String, List<Integer>> getMultiplePeaks(int[] arr) {
        List<Integer> pos = new ArrayList<>();
        List<Integer> pea = new ArrayList<>();
        Map<String, List<Integer>> ma = new HashMap<>();
        int cur = 0, pre = 0;
        for (int a = 1; a < arr.length; a++) {
            if (arr[a] > arr[cur]) {
                pre = cur;
                cur = a;
            } else {
                if (arr[a] < arr[cur])
                    if (arr[pre] < arr[cur]) {
                        pos.add(cur);
                        pea.add(arr[cur]);
                    }
                pre = cur;
                cur = a;
            }

        }
        ma.put("pos", pos);
        ma.put("peaks", pea);
        return ma;
    }

    public static List<AdcHit> decodePayload(BigInteger frame_time_ns, byte[] payload) {
        List<AdcHit> res = new ArrayList<>();
        ByteBuffer bb = ByteBuffer.wrap(payload);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        int[] slot_ind = new int[8];
        int[] slot_len = new int[8];
        long tag = EUtil.getUnsignedInt(bb);
        if ((tag & 0x8FFF8000L) == 0x80000000L) {

            for (int jj = 0; jj < 8; jj++) {
                slot_ind[jj] = EUtil.getUnsignedShort(bb);
                slot_len[jj] = EUtil.getUnsignedShort(bb);
            }
            for (int i = 0; i < 8; i++) {
                if (slot_len[i] > 0) {
                    bb.position(slot_ind[i] * 4);
                    int type = 0;
                    for (int j = 0; j < slot_len[i]; j++) {
                        int val = bb.getInt();
                        AdcHit hit = new AdcHit();

                        if ((val & 0x80000000) == 0x80000000) {
                            type = (val >> 15) & 0xFFFF;
                            hit.setCrate((val >> 8) & 0x007F);
                            hit.setSlot((val) & 0x001F);
                        } else if (type == 0x0001) /* FADC hit type */ {
                            hit.setQ((val) & 0x1FFF);
                            hit.setChannel((val >> 13) & 0x000F);
                            long v = ((val >> 17) & 0x3FFF) * 4;
                            BigInteger ht = BigInteger.valueOf(v);
                            hit.setTime(frame_time_ns.add(ht));
                            hit.setTime(ht);
                            res.add(hit);
                        }
                    }
                }
            }
        } else {
            System.out.println("parser error: wrong tag");
            System.exit(0);
        }
        return res;
    }

    public static void testByteBufferClone(String name, ByteBuffer b) {
        System.out.println(name + ": position = " + b.position() +
                " limit = " + b.limit() +
                " capacity = " + b.capacity() +
                " order = " + b.order());
    }

    public static void decodePayloadMap2(Long frame_time_ns, ByteBuffer buf) {
        buf.rewind();
        List<Integer> pData = new ArrayList<>();
        while (buf.hasRemaining()) {
            pData.add(buf.getInt());
        }
        if (!pData.isEmpty()) {
            if ((pData.get(0) & 0x8FFF8000) == 0x80000000) {
                for (int j = 1; j < 9; j++) {
                    int vl = pData.get(j);
                    int slot_ind = (vl >> 0) & 0xFFFF;
                    int slot_len = (vl >> 16) & 0xFFFF;
                    if (slot_len > 0) {
                        int type = 0x0;
                        int crate = -1;
                        int slot = -1;
                        for (int jj = 0; jj < slot_len; jj++) {
                            int val = pData.get(slot_ind + jj);

                            if ((val & 0x80000000) == 0x80000000) {
                                type = (val >> 15) & 0xFFFF;
                                crate = (val >> 8) & 0x007F;
                                slot = (val >> 0) & 0x001F;
                            } else if (type == 0x0001) { // FADC hit type
                                int q = (val >> 0) & 0x1FFF;
                                int channel = (val >> 13) & 0x000F;
                                long v = ((val >> 17) & 0x3FFF) * 4;
                                long ht = frame_time_ns + v;
//                                System.out.println("AdcHit{" +
//                                        "crate=" + crate +
//                                        ", slot=" + slot +
//                                        ", channel=" + channel +
//                                        ", q=" + q +
//                                        ", time=" + ht +
//                                        '}');
                            }
                        }
                    }
                }
            } else {
                System.out.println("parser error: wrong tag");
                System.exit(0);
            }
        }
    }

    public static void decodePayloadMap3(Long frame_time_ns, ByteBuffer buf, int s1, int s2) {
        buf.rewind();
        List<Integer> pData = new ArrayList<>();
        while (buf.hasRemaining()) {
            pData.add(buf.getInt());
        }
        corePayloadDecoder(frame_time_ns, pData, s1);
        corePayloadDecoder(frame_time_ns, pData, s2);
    }

    private static void corePayloadDecoder(Long frame_time_ns,
                                           List<Integer> pData, int pIndex) {
        if (!pData.isEmpty()) {
            if ((pData.get(pIndex) & 0x8FFF8000) == 0x80000000) {
                for (int j = pIndex + 1; j < pIndex + 9; j++) {
                    int vl = pData.get(j);
                    int slot_ind = (vl >> 0) & 0xFFFF;
                    int slot_len = (vl >> 16) & 0xFFFF;
                    if (slot_len > 0) {
                        int type = 0x0;
                        int crate = -1;
                        int slot = -1;
                        for (int jj = 0; jj < slot_len; jj++) {
                            int val = pData.get(slot_ind + pIndex + jj);

                            if ((val & 0x80000000) == 0x80000000) {
                                type = (val >> 15) & 0xFFFF;
                                crate = (val >> 8) & 0x007F;
                                slot = (val >> 0) & 0x001F;
                            } else if (type == 0x0001) { // FADC hit type
                                int q = (val >> 0) & 0x1FFF;
                                int channel = (val >> 13) & 0x000F;
                                long v = ((val >> 17) & 0x3FFF) * 4;
                                long ht = frame_time_ns + v;
//                                new RingAdcHitEvent().addHit(ht,
//                                        new AdcHit(crate, slot, channel, q, BigInteger.valueOf(ht)));

//                                System.out.println("AdcHit{" +
//                                        "crate=" + crate +
//                                        ", slot=" + slot +
//                                        ", channel=" + channel +
//                                        ", q=" + q +
//                                        ", time=" + ht +
//                                        '}');

                            }
                        }
                    }
                }
            } else {
//                System.out.println("parser error: wrong tag");
//                System.exit(0);
            }
        }

    }

    public static ByteBuffer cloneByteBuffer(final ByteBuffer original) {

        // Create clone with same capacity as original.
        final ByteBuffer clone = (original.isDirect()) ?
                ByteBuffer.allocateDirect(original.capacity()) :
                ByteBuffer.allocate(original.capacity());

        original.rewind();
        clone.put(original);
        clone.flip();
        clone.order(original.order());
        return clone;
    }

    public static void printFrame(int streamId, int source_id, int total_length, int payload_length,
                                  int compressed_length, int magic, int format_version,
                                  int flags, long record_number, long ts_sec, long ts_nsec) {
        System.out.println("\n================");
        System.out.println(streamId + ":source ID = " + source_id);
        System.out.println(streamId + ":total_length = " + total_length);
        System.out.println(streamId + ":payload_length = " + payload_length);
        System.out.println(streamId + ":compressed_length = " + compressed_length);
        System.out.println(String.format(streamId + ":magic = %x", magic));
        System.out.println(streamId + ":format_version = " + format_version);
        System.out.println(streamId + ":flags = " + flags);
        System.out.println(streamId + ":record_number = " + record_number);
        System.out.println(streamId + ":ts_sec = " + ts_sec);
        System.out.println(streamId + ":ts_nsec = " + ts_nsec);
    }

    public static void printHits(Map<Integer, List<ChargeTime>> hits) {
        hits.forEach((k, v) -> {
            System.out.println("crate = " + decodeCrateNumber(k)
                    + " slot = " + decodeSlotNumber(k)
                    + " channel = " + decodeChannelNumber(k));
            v.forEach(h -> System.out.println("t = " + h.getTime()
                    + " charge = " + h.getCharge()));
            System.out.println();
        });

    }

    public static void decodeSampaSerial(int eLink, int[] gbt_frame_t) {

        Vector<Vector<Integer>> eLinkDataTemp = new Vector<>(28);
        Vector<Vector<Integer>> eLinkData = new Vector<>(28);

        int[] syncFoundCount = new int[28];
        int[] syncLostCount = new int[28];
        int[] syncCount = new int[28];
        int[] heartBeatCount = new int[28];
        int[] dataHeaderCount = new int[28];
        int[] dataChannelCount = new int[160];

        long syncHeaderPattern = 0x1555540F00113L;        // sync packet header pattern

        long[] shiftReg = new long[28]; // static variables for 28 elink serial streams
        int[] syncFound = new int[28];
        int[] dataHeader = new int[28];
        int[] headerBitCount = new int[28];
        int[] dataBitCount = new int[28];
        int[] dataWordCount = new int[28];
        int[] dataCount = new int[28];
        int[] numWords = new int[28];

        int bitValue = 0;
        int dataWord = 0;
        int pkt = 0;
        int hadd = 0;
        int chadd = 0;
        int bxCount = 0;
        int hamming = 0;
        int parity = 0;
        int dataParity = 0;

        int head1 = 0;
        int head2 = 0;
        int dataValue = 0;

        int gFrameWord = 0;

        int ii_min = 0;
        int ii_max = 0;

        int fec_channel = 0;

        boolean match = false;

        int tempData = 0;

        if (eLink < 8)
            gFrameWord = gbt_frame_t[0];
        else if ((eLink >= 8) && (eLink < 16))
            gFrameWord = gbt_frame_t[1];
        else if ((eLink >= 16) && (eLink < 24))
            gFrameWord = gbt_frame_t[2];
        else
            gFrameWord = gbt_frame_t[3];

        ii_min = (eLink % 8) * 4;
        ii_max = ii_min + 3;

        if (syncFound[eLink] == 0)                    // find sync header - this will run until first sync packet header is found
        {
            System.out.println("DDD ============== "+ ii_min +" "+ ii_max);
            for (int ii = ii_max; ii >= ii_min; ii--)        // elink (4 bits per frame)
            {
                bitValue = (gFrameWord & (1 << ii)) >> ii;
                if (bitValue == 1)
                    shiftReg[eLink] = shiftReg[eLink] | 0x0004000000000000L;        // set bit 50 in shiftReg
                shiftReg[eLink] = shiftReg[eLink] >> 1;

                if (syncFound[eLink] == 1) {                   // when sync found count remaining bits of frame for next header
                    headerBitCount[eLink] = headerBitCount[eLink] + 1;
                }
                if (shiftReg[eLink] == syncHeaderPattern)    // check if sync packet header detected
                {
                    syncFound[eLink] = 1;
                    syncFoundCount[eLink]++;
                    syncCount[eLink]++;
                    headerBitCount[eLink] = 0;
                }
            }
            if (syncFound[eLink] == 1)                    // print headerBitCount after frame where sync packet found
            {
                System.out.println("DDD: SyncPacket found headerButCount = " + headerBitCount[eLink]);
            }
        } else if (dataHeader[eLink] == 0)                // runs only after first sync packet header has been found
        {                                                // we find NEXT header here
            for (int ii = ii_max; ii >= ii_min; ii--)        // elink 0 (4 bits per frame)
            {
                bitValue = (gFrameWord & (1 << ii)) >> ii;
                if (bitValue == 1)
                    shiftReg[eLink] = shiftReg[eLink] | 0x0004000000000000L;        // set bit 50 in shiftReg
                shiftReg[eLink] = shiftReg[eLink] >> 1;

                if (dataHeader[eLink] == 1)                // AFTER data header is found count remaining bits of frame as data bits
                    dataBitCount[eLink] = dataBitCount[eLink] + 1;
                else                            // count frame bits as header bitsis not data type
                    headerBitCount[eLink] = headerBitCount[eLink] + 1;

//          -----------------------------------------------------------------------
                if (headerBitCount[eLink] == 50)        // next packet header - decode
                {
                    if (shiftReg[eLink] == syncHeaderPattern)            // sync header
                    {
                        syncCount[eLink]++;
                        headerBitCount[eLink] = 0;

                        System.out.println("DDD: SYNC HEADER  elink = " + eLink +
                                " shiftReg = " + shiftReg[eLink] +
                                " syncCount = " + syncCount[eLink]);
                    } else                                                // non-sync packet header - identify type
                    {
                        pkt = (int) ((shiftReg[eLink] >> 7) & 0x7);
                        numWords[eLink] = (int) ((shiftReg[eLink] >> 10) & 0x3FF);
                        hadd = (int) ((shiftReg[eLink] >> 20) & 0xF);
                        chadd = (int) ((shiftReg[eLink] >> 24) & 0x1F);
                        bxCount = (int) ((shiftReg[eLink] >> 29) & 0xFFFFF);
                        hamming = (int) ((shiftReg[eLink]) & 0x3F);
                        dataParity = (int) ((shiftReg[eLink] >> 49) & 0x1);
                        parity = (int) ((shiftReg[eLink] >> 6) & 0x1);

                        if ((pkt == 0) && (numWords[eLink] == 0) && (chadd == 0x15))        // heartbeat packet (NO payload) - push into output stream
                        {
                            heartBeatCount[eLink]++;
                            headerBitCount[eLink] = 0;
                            head1 = 0xA0000000 | (bxCount << 9) | (chadd << 4) | hadd;
                            head2 = 0x40000000 | (parity << 23) | (hamming << 17) | (dataParity << 16) | (numWords[eLink] << 3) | pkt;
                            System.out.println("DDD: HEARTBEAT HEADER  elink = " + eLink
                                    + " shiftReg = " + shiftReg[eLink] +
                                    " heartBeatCount = " + heartBeatCount[eLink]);
                        } else if (pkt == 4)                // initially require only NORMAL data packet headers
                        {
                            // check consistency of data header - verify that 'hadd' and chadd' are consistent with 'eLink' number
                            match = matchSampaDataHeader(eLink, hadd, chadd);

                            if (match)                    // header consistent with data header
                            {
                                dataCount[eLink] = dataCount[eLink] + 1;
                                dataHeaderCount[eLink]++;
                                fec_channel = (int) (hadd * 32 + chadd);
                                if ((fec_channel >= 0) && (fec_channel <= 159)) {
                                    dataChannelCount[fec_channel]++;
                                } else {
                                    System.out.println("DDD:  ILLEGAL CHANNEL NUMBER  elink = " + eLink +
                                            " hadd = " + hadd + " chadd = " + chadd);
                                }
                                dataHeader[eLink] = 1;
                                dataBitCount[eLink] = 0;
                                dataWordCount[eLink] = 0;
                                headerBitCount[eLink] = 0;
                                head1 = 0xA0000000 | (bxCount << 9) | (chadd << 4) | hadd;
                                head2 = 0x40000000 | (parity << 23) | (hamming << 17) | (dataParity << 16) | (numWords[eLink] << 3) | pkt;

                                eLinkDataTemp.get(eLink).add(head1);        // push header into temporary storage vector
                                eLinkDataTemp.get(eLink).add(head2);

                                System.out.println("DDD: DATA HEADER  elink = " + eLink +
                                        " shiftReg = " + shiftReg[eLink] +
                                        " pkt = " + pkt +
                                        " dataCount = " + dataCount[eLink] +
                                        " numWords = " + numWords[eLink] +
                                        " hadd = " + hadd +
                                        " chadd = " + chadd +
                                        " bxCount = " + bxCount);
                            } else                            // inconsistent data header - force the finding of next sync header
                            {
                                headerBitCount[eLink] = 0;
                                syncFound[eLink] = 0;
                                syncLostCount[eLink]++;
                                System.out.println("DDD: UNRECOGNIZED HEADER  elink = " + eLink +
                                        " shiftReg = " + shiftReg[eLink] +
                                        " pkt = ");
                            }
                        } else                            // 'unrecognized' header - force the finding of next sync header
                        {
                            headerBitCount[eLink] = 0;
                            syncFound[eLink] = 0;
                            syncLostCount[eLink]++;
                            System.out.println("DDD: UNRECOGNIZED HEADER  elink = " + eLink +
                                    " shiftReg = " + shiftReg[eLink] +
                                    " pkt = " + pkt);
                        }
                    }
                }
                //-----------------------------------------------------------------------
            }
        } else if (dataHeader[eLink] == 1)                // runs only after data packet header has been found
        {
            for (int ii = ii_max; ii >= ii_min; ii--)        // elink (4 bits per frame)
            {
                bitValue = (gFrameWord & (1 << ii)) >> ii;
                if (bitValue == 1)
                    shiftReg[eLink] = shiftReg[eLink] | 0x0004000000000000L;        // set bit 50 in shiftReg
                shiftReg[eLink] = shiftReg[eLink] >> 1;

                if (dataHeader[eLink] == 1)                // count data word bits until data payload is exhausted
                    dataBitCount[eLink] = dataBitCount[eLink] + 1;
                else                            // if payload is exhausted count remaining bits of frame for next header
                    headerBitCount[eLink] = headerBitCount[eLink] + 1;

                if (dataBitCount[eLink] == 10)        // print data word
                {
                    dataWordCount[eLink] = dataWordCount[eLink] + 1;
                    dataWord = (int) ((shiftReg[eLink] >> 40) & 0x3FF);
                    dataValue = (dataWordCount[eLink] << 16) | dataWord;
                    eLinkDataTemp.get(eLink).add(dataValue);        // push data into temporary storage vector

                    System.out.println("DDD:  shiftReg = " + shiftReg[eLink] +
                            " data(hex) = " + String.format("0x%08X", dataWord) +
                            " data = " + dataWord +
                            " dataWordCount = " + dataWordCount[eLink] +
                            " elink = " + eLink);
                    dataBitCount[eLink] = 0;
                }

                if (dataWordCount[eLink] == numWords[eLink])        // done with packet payload
                {
                    // Both header words and all packet data words have been stored in a temporary vector.
                    // This is done to assure that only complete packets appear in the output data stream.
                    // Now copy the temporary vector to the output stream.
                    for (int jj = 0; jj < (numWords[eLink] + 2); jj++) {
                        tempData = eLinkDataTemp.get(eLink).get(jj);
                        eLinkData.get(eLink).add(tempData);        // copy temp data into output stream vector
                    }
                    eLinkDataTemp.get(eLink).clear();                // delete all data of temporary vector

                    dataHeader[eLink] = 0;                    // reset
                    headerBitCount[eLink] = 0;
                    dataBitCount[eLink] = 0;
                    dataWordCount[eLink] = 0;
                    System.out.println("DDD: END OF DATA  elink = " + eLink);
                }
            }
        }
    }

    public static boolean matchSampaDataHeader(int eLink, int hadd, int chadd) {
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
        if (eLink < 11)            // eLink 0-10 are from chip 0 (link00) or chip 3 (link01)
        {
            chip_a = 0;
            chip_b = 3;
        } else if (eLink < 22)        // eLink 11-21 are from chip 1 (link00) or chip 4 (link01)
        {
            chip_a = 1;
            chip_b = 4;
        } else                        // eLink 22-27 are from chip 2 (link00, link01)
        {
            chip_a = 2;
            chip_b = 2;
        }

        if (eLink < 10)            // compare to 6 possible channel values due to complex chip 2 mapping
        {
            chan_a = (eLink % 11) * 3;    // eLink 0 (ch 0,1,2), eLink 1 (ch 3,4,5), ... elink 9 (ch 27,28,29)
            chan_b = chan_a + 1;
            chan_c = chan_a + 2;
            chan_d = chan_a;
            chan_e = chan_b;
            chan_f = chan_c;
        } else if (eLink == 10)            // eLink 10 (ch 30,31)
        {
            chan_a = 30;
            chan_b = 31;
            chan_c = 30;
            chan_d = 31;
            chan_e = 30;
            chan_f = 31;
        }
        if (eLink < 21)                // eLink 11 (ch 0,1,2), eLink 12 (ch 3,4,5), ... elink 20 (ch 27,28,29)
        {
            chan_a = (eLink % 11) * 3;
            chan_b = chan_a + 1;
            chan_c = chan_a + 2;
            chan_d = chan_a;
            chan_e = chan_b;
            chan_f = chan_c;
        } else if (eLink == 21)            // eLink 21 (ch 30,31)
        {
            chan_a = 30;
            chan_b = 31;
            chan_c = 30;
            chan_d = 31;
            chan_e = 30;
            chan_f = 31;
        } else if (eLink < 27)        // Link00:  eLink 22 (ch 0,1,2),    ... elink 26 (ch 12,13,14)
        {                            // Link01:  eLink 22 (ch 15,16,17), ... elink 26 (ch 27,28,29)
            chan_a = (eLink % 22) * 3;
            chan_b = chan_a + 1;
            chan_c = chan_a + 2;
            chan_d = chan_a + 15;
            chan_e = chan_b + 15;
            chan_f = chan_c + 15;
        } else                        // eLink 27 (ch 30,31)
        {
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


}


