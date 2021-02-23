
#ifndef ERSAP_VTP_RINGEVENT_H
#define ERSAP_VTP_RINGEVENT_H


#include <cstdlib>
#include <memory>
#include <functional>

#include "ByteOrder.h"
#include "ByteBuffer.h"


namespace ersap {


    class RingEvent {

    private:
        int streamId = 0;
        int64_t recordNumber = 0L;
        int64_t payloadSize;
        uint8_t * payload;
        std::shared_ptr<ByteBuffer> payloadBuffer;
        int payloadDataLength = 0;

    public:

        /** Function used to create (shared pointers to) RingEvent objects by a RingBuffer. */
        static const std::function< std::shared_ptr<RingEvent> () >& eventFactory();

        /** Constructor. */
        RingEvent() {
            payloadSize = 100000L;
            payloadBuffer = std::make_shared<ByteBuffer>(payloadSize);
            payloadBuffer->order(ByteOrder::ENDIAN_LOCAL);
            payload = payloadBuffer->array();
        }

        /**
         * Copy constructor.
         * @param other ring event to copy.
         */
        RingEvent(const RingEvent & other) {

            // Avoid self copy ...
            if (this != &other) {
                streamId          = other.streamId;
                recordNumber      = other.recordNumber;
                payloadSize       = other.payloadSize;
                payloadDataLength = other.payloadDataLength;

                // COPY contents of payloadBuffer, DON'T just change value of shared pointer
                payloadBuffer->copy(other.payloadBuffer);
                payload = payloadBuffer->array();
            }
        }

        /**
          * Assignment operator.
          * Used to copy RingEvent from input ring to output ring.
          * @param other right side object.
          * @return left side object.
          */
        RingEvent & operator=(const RingEvent& other) {

            // Avoid self assignment ...
            if (this != &other) {
                streamId          = other.streamId;
                recordNumber      = other.recordNumber;
                payloadSize       = other.payloadSize;
                payloadDataLength = other.payloadDataLength;

                // COPY contents of payloadBuffer, DON'T just change value of shared pointer
                payloadBuffer->copy(*other.payloadBuffer);
                payload = payloadBuffer->array();
            }
            return *this;
        }


        int getStreamId() const {return streamId;}
        void setStreamId(int sId) {streamId = sId;}

        int64_t getRecordNumber() const {return recordNumber;}
        void setRecordNumber(int64_t recNumber) {recordNumber = recNumber;}

        int64_t getPayloadSize() const {return payloadSize;}
        void setPayloadSize(int64_t size) {payloadSize = size;}

        int getPayloadDataLength() const {return payloadDataLength;}
        void setPayloadDataLength(int payldDataLen) {payloadDataLength = payldDataLen;}

        uint8_t * getPayload() {return payload;}

        std::shared_ptr<ByteBuffer> getPayloadBuffer() {return payloadBuffer;}
        void setPayloadBuffer(std::shared_ptr<ByteBuffer> & buf, int64_t size) {
            payloadSize = size;
            payloadBuffer = buf;
            payload = payloadBuffer->array();
        }

        void increaseSize(int64_t bytes) {
            if (bytes < payloadSize) return;
            payloadSize = bytes;
            payloadBuffer = std::make_shared<ByteBuffer>(payloadSize);
            payload = payloadBuffer->array();
        }
    };

}

#endif  // ERSAP_VTP_RINGEVENT_H

