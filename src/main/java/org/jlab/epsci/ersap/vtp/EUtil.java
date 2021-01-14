package org.jlab.epsci.ersap.vtp;


import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * ERSAP utility class
 *
 * @author gurjyan
 * @since 04.01.20
 */
public class EUtil {
    private static final byte[] i32 = new byte[4];
    private static final byte[] i64 = new byte[8];

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

    public static long readLteUnsined32(DataInputStream dataInputStream) {
        ByteBuffer bb = null;
//        byte[] i32 = new byte[4];
        try {
            dataInputStream.readFully(i32);
            bb = ByteBuffer.wrap(i32);
            bb.order(ByteOrder.LITTLE_ENDIAN);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert bb != null;
        return getUnsignedInt(bb);
    }

    public static int readUnsined32(DataInputStream dataInputStream) throws IOException {
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

    public static long[] readLtPayload(DataInputStream dataInputStream, long payload_length) {
        long[] payload = new long[(int) payload_length / 4];
        int j = 0;
        for (long i = 0; i < payload_length; i = i + 4) {
            payload[j] = readLteUnsined32(dataInputStream);
            j = j + 1;
        }
        return payload;
    }

    public static long llSwap(long l) {
        long x = l >> 32;
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
        return csc & 0x000000f0;
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

    public static  List<AdcHit> decodePayload(BigInteger frame_time_ns, byte[] payload) {
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

    public static  Map<BigInteger,List<AdcHit>> decodePayloadMap2(Long frame_time_ns, ByteBuffer payloadBuffer, List<Integer> pData) {
//        Map<BigInteger,List<AdcHit>> res = new HashMap<>();

        // read entire buffer into an array
        while (payloadBuffer.hasRemaining()) {
            System.out.println("DDD ");
            pData.add(payloadBuffer.getInt());
        }
        if( (pData.get(0) & 0x8FFF8000) == 0x80000000 ) {
            for(int j=1; j<9; j++) {
                int vl = pData.get(j);
                int slot_ind = (vl>>0)  & 0xFFFF;
                int slot_len = (vl>>16) & 0xFFFF;
                if(slot_len > 0) {
                    int type = 0x0;
                    int crate  = -1;
                    int slot = -1;
                    for(int jj=0; jj<slot_len; jj++) {
                         int val = pData.get(slot_ind + jj);

                        if ((val & 0x80000000) == 0x80000000) {
                            type  = (val>>15) & 0xFFFF;
                            crate = (val >> 8) & 0x007F;
                            slot = (val >> 0) & 0x001F;
                        }
                        else if(type == 0x0001) { // FADC hit type
                            ///ddddd
                            int q = (val >> 0) & 0x1FFF;
                            int channel = (val >> 13) & 0x000F;
                              long v = ((val >> 17) & 0x3FFF) * 4;
                            long ht = frame_time_ns + v;
                            // ddddd

//                            AdcHit hit = new AdcHit();
//                            hit.setCrate(crate);
//                            hit.setSlot(slot);
//                            hit.setQ((val >> 0) & 0x1FFF);
//                            hit.setChannel((val >> 13) & 0x000F);
//                            long v = ((val >> 17) & 0x3FFF) * 4;
//                            BigInteger ht = BigInteger.valueOf(v);
//                            hit.setTime(frame_time_ns.add(ht));
//                            if(res.containsKey(ht)){
//                                res.get(ht).add(hit);
//                            } else {
//                                List<AdcHit> adcHits = new ArrayList<>();
//                                adcHits.add(hit);
//                                res.put(ht, adcHits);
//                            }
                        }
                    }
                }
            }
        } else {
            System.out.println("parser error: wrong tag");
            System.exit(0);
        }
        return null;
//        return res;
    }


    public static void printFrame(int streamId, int source_id, int total_length, int payload_length,
                                  int compressed_length, int magic, int format_version,
                                  int flags, long record_number, long ts_sec, long ts_nsec) {
        System.out.println("\n================");
        System.out.println(streamId+":source ID = " + source_id);
        System.out.println(streamId+":total_length = " + total_length);
        System.out.println(streamId+":payload_length = " + payload_length);
        System.out.println(streamId+":compressed_length = " + compressed_length);
        System.out.println(String.format(streamId+":magic = %x", magic));
        System.out.println(streamId+":format_version = " + format_version);
        System.out.println(streamId+":flags = " + flags);
        System.out.println(streamId+":record_number = " + record_number);
        System.out.println(streamId+":ts_sec = " + ts_sec);
        System.out.println(streamId+":ts_nsec = " + ts_nsec);
    }

    public static void printHits(Map<Integer, List<ChargeTime>> hits){
        hits.forEach((k,v) ->  {
            System.out.println("crate = " + decodeCrateNumber(k)
                    + " slot = "+decodeSlotNumber(k)
                    + " channel = "+decodeChannelNumber(k));
            v.forEach(h -> System.out.println("t = " + h.getTime()
                    + " charge = "+ h.getCharge()));
            System.out.println();
        });

    }

}
