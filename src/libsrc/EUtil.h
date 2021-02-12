
#ifndef ERSAP_VTP_EUTIL_H
#define ERSAP_VTP_EUTIL_H


#include <cstdlib>
#include <string>
#include <stdexcept>
#include <thread>
#include <chrono>
#include <unordered_map>
#include <utility>
#include <vector>

#include "ByteBuffer.h"
#include "ByteOrder.h"
#include "RingEvent.h"
#include "ChargeTime.h"



namespace ersap {

    /**
     * ERSAP utility class
     *
     * @author gurjyan
     * @since 04.01.20
     */
    class EUtil {

    private:

        static std::shared_ptr<ByteBuffer> bb32;
        static std::shared_ptr<ByteBuffer> bb64;

        static uint8_t *i32;
        static uint8_t *i64;

    public:

        /**
         * Returns unsigned byte for a ByteBuffer
         *
         * @param bb input ByteBuffer
         * @return unsigned byte as a short
         */
        static uint8_t getUnsignedByte(const ByteBuffer & bb) {
            return bb.getByte();
        }

        static void putUnsignedByte(ByteBuffer & bb, int value) {
            bb.put((uint8_t) (value & 0xff));
        }

        static uint8_t getUnsignedByte(const ByteBuffer & bb, int position) {
            return bb.getByte(position);
        }

        static void putUnsignedByte(ByteBuffer & bb, int position, int value) {
            bb.put(position, (uint8_t) (value & 0xff));
        }

        static uint16_t getUnsignedShort(const ByteBuffer & bb) {
            return bb.getUShort();
        }

        static void putUnsignedShort(ByteBuffer & bb, int value) {
            bb.putShort((uint16_t) (value & 0xffff));
        }

        static uint16_t getUnsignedShort(const ByteBuffer & bb, int position) {
            return bb.getUShort(position);
        }

        static void putUnsignedShort(ByteBuffer & bb, int position, int value) {
            bb.putShort(position, (uint16_t) (value & 0xffff));
        }


        static uint32_t getUnsignedInt(const ByteBuffer & bb) {
            return bb.getUInt();
        }

        static void putUnsignedInt(ByteBuffer & bb, long value) {
            bb.putInt((uint32_t) (value & 0xffffffffL));
        }

        static uint32_t getUnsignedInt(const ByteBuffer & bb, int position) {
            return bb.getUInt(position);
        }

        static void putUnsignedInt(ByteBuffer & bb, int position, long value) {
            bb.putInt(position, (uint32_t) (value & 0xffffffffL));
        }

//        static uint32_t readLteUnsigned32(DataInputStream dataInputStream) {
//             bb32->order(ByteOrder::ENDIAN_LITTLE);
//             dataInputStream.readFully(i32);
//            return EUtil::getUnsignedInt(*(bb32));
//        }
//
//        static uint32_t readUnsigned32(DataInputStream dataInputStream) {
//            int ch1 = dataInputStream.read() & 0xff;
//            int ch2 = dataInputStream.read() & 0xff;
//            int ch3 = dataInputStream.read() & 0xff;
//            int ch4 = dataInputStream.read() & 0xff;
//            return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
//        }
//
//
//        /**
//         * Avoid the creation of long[] arrays by providing as an arg.
//         * @param dataInputStream
//         * @param payload array to be filled
//         * @param wordLength number of 32-bit ints (words) to be read into payload
//         */
//        static void readLtPayload(DataInputStream dataInputStream, uint32_t *payload, size_t wordLength) {
//            int j = 0;
//            for (long i = 0; i < wordLength; i = i + 4) {
//                payload[j] = readLteUnsigned32(dataInputStream);
//                j = j + 1;
//            }
//        }
//
//
//        /**
//         * Must free the returned pointer when finished.
//         * @param dataInputStream
//         * @param payload_length number of bytes to read
//         * @return
//         */
//        static uint32_t * readLtPayload(DataInputStream dataInputStream, size_t payload_length) {
//            auto payload = new uint32_t[payload_length / 4];
//            int j = 0;
//            for (long i = 0; i < payload_length; i = i + 4) {
//                payload[j] = readLteUnsigned32(dataInputStream);
//                j = j + 1;
//            }
//            return payload;
//        }

        static uint64_t llSwap(uint64_t l) {
            uint64_t x = (l >> 32) & 0xffffffff;
            x = x | l << 32;
            return x;
        }

        /**
         * Caller supplies byte array.
         * @param lng
         * @param ba
         */
        static void long2ByteArray(uint64_t lng, uint8_t * ba) {
            ByteOrder::byteSwap64(& lng, 1, reinterpret_cast<uint64_t *>(ba));
        }

        /**
         * Caller must free returned pointer.
         * @param lng
         * @return
         */
        static uint8_t * long2ByteArray(uint64_t lng) {
            auto b = new uint8_t[] {
                    (uint8_t) lng,
                    (uint8_t) (lng >> 8),
                    (uint8_t) (lng >> 16),
                    (uint8_t) (lng >> 24),
                    (uint8_t) (lng >> 32),
                    (uint8_t) (lng >> 40),
                    (uint8_t) (lng >> 48),
                    (uint8_t) (lng >> 56)};
            return b;
        }

        static void busyWaitMicros(long delay) {
            std::this_thread::sleep_for(std::chrono::microseconds(delay));
        }

//        static <T> T requireNonNull(T obj, String desc) {
//            return Objects.requireNonNull(obj, "null " + desc);
//        }

        static void addByteArrays(uint8_t * a, int aLength, uint8_t *  b, int bLength, uint8_t * c) {
            std::memcpy(c, a, aLength);
            std::memcpy(c + aLength, b, bLength);
        }

        static int encodeCSC(int crate, int slot, int channel) {
            return (crate << 16) | (slot << 8) | (channel << 4);
        }

        static int decodeCrateNumber(int csc) {
            return (csc >> 16) & 0xffff;
        }

        static int decodeSlotNumber(int csc) {
            return (csc >> 8) & 0xf;
        }

        static int decodeChannelNumber(int csc) {
            return csc & 0xf;
        }


        static std::unordered_map<std::string, std::vector<int>> getMultiplePeaks(int32_t *arr, uint32_t arrLen) {
            std::vector<int32_t> pos;
            std::vector<int32_t> pea;
            std::unordered_map<std::string, std::vector<int>> ma;

            int32_t cur = 0, pre = 0;
            for (int a = 1; a < arrLen; a++) {
                if (arr[a] > arr[cur]) {
                    pre = cur;
                    cur = a;
                } else {
                    if (arr[a] < arr[cur])
                        if (arr[pre] < arr[cur]) {
                            pos.push_back(cur);
                            pea.push_back(arr[cur]);
                        }
                    pre = cur;
                    cur = a;
                }

            }

            ma["pos"] = pos;
            ma["peaks"] = pea;
            return std::move(ma);
        }

//        static std::vector<AdcHit> decodePayload(BigInteger frame_time_ns, byte[] payload) {
//            List<AdcHit> res = new ArrayList<>();
//            ByteBuffer bb = ByteBuffer.wrap(payload);
//            bb.order(ByteOrder.LITTLE_ENDIAN);
//            int[] slot_ind = new int[8];
//            int[] slot_len = new int[8];
//            long tag = EUtil.getUnsignedInt(bb);
//            if ((tag & 0x8FFF8000L) == 0x80000000L) {
//
//                for (int jj = 0; jj < 8; jj++) {
//                    slot_ind[jj] = EUtil.getUnsignedShort(bb);
//                    slot_len[jj] = EUtil.getUnsignedShort(bb);
//                }
//                for (int i = 0; i < 8; i++) {
//                    if (slot_len[i] > 0) {
//                        bb.position(slot_ind[i] * 4);
//                        int type = 0;
//                        for (int j = 0; j < slot_len[i]; j++) {
//                            int val = bb.getInt();
//                            AdcHit hit = new AdcHit();
//
//                            if ((val & 0x80000000) == 0x80000000) {
//                                type = (val >> 15) & 0xFFFF;
//                                hit.setCrate((val >> 8) & 0x007F);
//                                hit.setSlot((val) & 0x001F);
//                            } else if (type == 0x0001) /* FADC hit type */ {
//                                hit.setQ((val) & 0x1FFF);
//                                hit.setChannel((val >> 13) & 0x000F);
//                                long v = ((val >> 17) & 0x3FFF) * 4;
//                                BigInteger ht = BigInteger.valueOf(v);
//                                hit.setTime(frame_time_ns.add(ht));
//                                hit.setTime(ht);
//                                res.add(hit);
//                            }
//                        }
//                    }
//                }
//            } else {
//                System.out.println("parser error: wrong tag");
//                System.exit(0);
//            }
//            return res;
//        }

        static void testByteBufferClone(std::string & name, ByteBuffer & b) {
            std::cout << name << ": position = " << b.position() <<
                               " limit = " << b.limit() <<
                               " capacity = " << b.capacity() <<
                               " order = " << b.order().getName() << std::endl;
        }

        static void decodePayloadMap2(int64_t frame_time_ns, ByteBuffer & buf) {
            buf.rewind();
            std::vector<int32_t> pData;

            while (buf.hasRemaining()) {
                pData.push_back(buf.getInt());
            }

            if (!pData.empty()) {
                if ((pData[0] & 0x8FFF8000) == 0x80000000) {
                    for (int j = 1; j < 9; j++) {
                        int32_t vl = pData[j];
                        int32_t slot_ind = (vl >> 0) & 0xFFFF;
                        int32_t slot_len = (vl >> 16) & 0xFFFF;
                        if (slot_len > 0) {
                            int32_t type = 0x0;
                            int32_t crate = -1;
                            int32_t slot = -1;
                            for (int jj = 0; jj < slot_len; jj++) {
                                int32_t val = pData[slot_ind + jj];

                                if ((val & 0x80000000) == 0x80000000) {
                                    type  = (val >> 15) & 0xFFFF;
                                    crate = (val >> 8 ) & 0x007F;
                                    slot  = (val >> 0 ) & 0x001F;
                                } else if (type == 0x0001) { // FADC hit type
                                    int32_t q = (val >> 0) & 0x1FFF;
                                    int32_t channel = (val >> 13) & 0x000F;
                                    int64_t v = ((val >> 17) & 0x3FFF) * 4;
                                    int64_t ht = frame_time_ns + v;
//                                    std::cout << "AdcHit{" <<
//                                             "crate=" << crate <<
//                                             ", slot=" << slot <<
//                                             ", channel=" << channel <<
//                                             ", q=" << q <<
//                                             ", time=" << ht <<
//                                             '}' << std::endl;
                                }
                            }
                        }
                    }
                } else {
                    std::cout << "parser error: wrong tag" << std::endl;
                    std::exit(0);
                }
            }
        }

        static void decodePayloadMap3(int64_t frame_time_ns, std::shared_ptr<ByteBuffer> buf) {
            buf->rewind();
            std::vector<int32_t> pData;

            while (buf->hasRemaining()) {
                pData.push_back(buf->getInt());
            }

            if (!pData.empty()) {
                if ((pData[0] & 0x8FFF8000) == 0x80000000) {
                    for (int j = 1; j < 9; j++) {
                        int32_t vl = pData[j];
                        int32_t slot_ind = (vl >> 0) & 0xFFFF;
                        int32_t slot_len = (vl >> 16) & 0xFFFF;
                        if (slot_len > 0) {
                            int32_t type = 0x0;
                            int32_t crate = -1;
                            int32_t slot = -1;
                            for (int jj = 0; jj < slot_len; jj++) {
                                int32_t val = pData[slot_ind + jj];

                                if ((val & 0x80000000) == 0x80000000) {
                                    type  = (val >> 15) & 0xFFFF;
                                    crate = (val >> 8 ) & 0x007F;
                                    slot  = (val >> 0 ) & 0x001F;
                                } else if (type == 0x0001) { // FADC hit type
                                    int32_t q = (val >> 0) & 0x1FFF;
                                    int32_t channel = (val >> 13) & 0x000F;
                                    int64_t v = ((val >> 17) & 0x3FFF) * 4;
                                    int64_t ht = frame_time_ns + v;
                                    //                                    std::cout << "AdcHit{" <<
                                    //                                             "crate=" << crate <<
                                    //                                             ", slot=" << slot <<
                                    //                                             ", channel=" << channel <<
                                    //                                             ", q=" << q <<
                                    //                                             ", time=" << ht <<
                                    //                                             '}' << std::endl;
                                }
                            }
                        }
                    }
                } else {
                    std::cout << "parser error: wrong tag" << std::endl;
                    std::exit(0);
                }
            }
        }

        static void printFrame(int streamId, int source_id, int total_length, int payload_length,
                               int compressed_length, int magic, int format_version,
                               int flags, long record_number, long ts_sec, long ts_nsec) {
            std::cout << std::endl << "================" << std::endl;
            std::cout << streamId << ":source ID = " << source_id << std::endl;
            std::cout << streamId << ":total_length = " << total_length << std::endl;
            std::cout << streamId << ":payload_length = " << payload_length << std::endl;
            std::cout << streamId << ":compressed_length = " << compressed_length << std::endl;
            std::cout << streamId << ":magic = 0x" << std::hex << magic << std::dec << std::endl;
            std::cout << streamId << ":format_version = " << format_version << std::endl;
            std::cout << streamId << ":flags = " << flags << std::endl;
            std::cout << streamId << ":record_number = " << record_number << std::endl;
            std::cout << streamId << ":ts_sec = " << ts_sec << std::endl;
            std::cout << streamId << ":ts_nsec = " << ts_nsec << std::endl;
        }

        static void printHits(std::unordered_map<int32_t, std::vector<ChargeTime>> & hits) {
            std::cout << "Iterate and print keys and values of unordered_map:\n";
            for ( const auto& n : hits ) {
                int32_t key = n.first;
                std::cout << "crate = " << decodeCrateNumber(key)
                             << " slot = " << decodeSlotNumber(key)
                             << " channel = " << decodeChannelNumber(key) << std::endl;
                for (const auto & val : n.second) {
                   std::cout << val.toString() << std::endl;
                }
                std::cout << std::endl;
            }
        }

    };


    // Initialization of static variables must be done outside of class definition

    std::shared_ptr<ByteBuffer> bb32 = std::make_shared<ByteBuffer>(4);
    std::shared_ptr<ByteBuffer> bb64 = std::make_shared<ByteBuffer>(8);

    uint8_t *i32 = bb32->array();
    uint8_t *i64 = bb64->array();

}

#endif //ERSAP_VTP_EUTIL_H
