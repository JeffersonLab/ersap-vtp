

#ifndef ERSAP_VTP_RECEIVER_H
#define ERSAP_VTP_RECEIVER_H


#include <string>
#include <stdexcept>
#include <thread>
#include <vector>
#include <iostream>
#include <chrono>


#include "ByteBuffer.h"
#include "RingEvent.h"
#include "EUtil.h"
#include "Disruptor/Disruptor.h"
#include <boost/thread.hpp>
#include <boost/asio.hpp>


using namespace Disruptor;
using namespace boost::asio;
using ip::tcp;

namespace ersap {

    /**
     * Receives stream frames from a VTP and writes them to a RingBuffer
     * <p>
     * ___        __
     * |   |      /  \
     * |   | ---> \  /
     * ---        --
     */
    class Receiver {

    private:

        /** Stream ID */
        int streamId;

        /** Output ring */
        std::shared_ptr<RingBuffer<std::shared_ptr<RingEvent>>> ringBuffer;

        /** Current spot in the ring from which an item was claimed. */
        int64_t getSequence = -1L;

        /** Thread which does the producing. */
        boost::thread thd;

        /** Buffer size. */
        size_t bufSize = 52;

        /** Buffer for reading into. */
        std::shared_ptr<ByteBuffer> headerBuffer;

        /** Array associated with buffer. */
        uint8_t * header;

        /** For boost I/O: mutable_buffers_1 takes a buffer and makes it into a MutableBufferSequence
         *  which is necessary for boost::asio::read. */
        std::shared_ptr<mutable_buffers_1> buffer;

        /** Socket to read from client. */
        std::shared_ptr<tcp::socket> socket;


        /** For statistics */
        int statLoop;
        int statPeriod;
        double totalData = 0.;
        int rate = 0;
        long missed_record = 0;
        long prev_rec_number = 0;


    public:

        Receiver(int vtpPort, int streamId,
                 std::shared_ptr<RingBuffer<std::shared_ptr<RingEvent>>> & ringBuffer,
                 int statPeriod) {

            this->ringBuffer = ringBuffer;
            this->streamId   = streamId;
            this->statPeriod = statPeriod;

            headerBuffer = std::make_shared<ByteBuffer>(bufSize);
            headerBuffer->order(ByteOrder::ENDIAN_LITTLE);
            header = headerBuffer->array();
            buffer = std::make_shared<mutable_buffers_1>((void *) header, bufSize);

            try {
                // Connecting to the VTP stream source
                io_service service;

                // listen for new connection
                tcp::acceptor acceptor(service, tcp::endpoint(tcp::v4(), vtpPort));
                std::cout << "Server is listening on port " << vtpPort << std::endl;

                // Socket creation as shared pointer so it can be class member and other methods can access it
                socket = std::make_shared<tcp::socket>(service);

                // waiting for connection
                acceptor.accept(*socket);
                std::cout << "VTP client connected" << std::endl;

                // read data from socket - skipping over first 2 ints
                readOnConnection();

            } catch (std::runtime_error &e) {
                std::cout << e.what() << std::endl;
            }

        }


        /** Skip over 2 ints initially sent on socket connection */
        void readOnConnection() {
            // Read in 8 bytes
            boost::asio::read(*socket, *buffer, boost::asio::transfer_exactly(8));
        }


        /** Read header from socket */
        void readHeader() {
            headerBuffer->clear();
            // Read in full size of header
            boost::asio::read(*socket, *buffer, boost::asio::transfer_exactly(bufSize));
            headerBuffer->limit(bufSize);
        }


        /** Read data from socket */
        void readData(std::shared_ptr<ByteBuffer> buf, size_t bytes) {
            buf->clear();
            // Turn shared pointer to ByteBuffer into usable form (MutableBufferSequence)
            mutable_buffers_1 mb((void *) buf->array(), bytes);
            // Read in full size of data
            boost::asio::read(*socket, mb, boost::asio::transfer_exactly(bytes));
            buf->limit(bytes);
        }


        /** Create and start a thread to execute the run() method of this class. */
        void startThread() {
            thd = boost::thread([this]() { this->run(); });
        }

        /** Stop the thread. */
        void stopThread() {
            // Send signal to interrupt it
            thd.interrupt();
            // Wait for it to stop
            thd.join();
        }



    private:

        /**
         * Get the next available item in ring buffer for writing data.
         * @return next available item in ring buffer.
         */
        std::shared_ptr<RingEvent> get() {
            getSequence = ringBuffer->next();
            return (*ringBuffer)[getSequence];
        }

        void publish() {
            ringBuffer->publish(getSequence);
        }


        void decodeVtpHeaderCT(std::shared_ptr<RingEvent> evt) {
            try {
                // Read header into headerBuffer
                readHeader();

                int source_id = headerBuffer->getInt();
                int total_length = headerBuffer->getInt();
                int payload_length = headerBuffer->getInt();
                int compressed_length = headerBuffer->getInt();
                int magic = headerBuffer->getInt();

                int format_version = headerBuffer->getInt();
                int flags = headerBuffer->getInt();
                long record_number = SWAP_64(headerBuffer->getLong());
                long ts_sec = SWAP_64(headerBuffer->getLong());
                long ts_nsec = SWAP_64(headerBuffer->getLong());

                // Read data into RingEvent's buffer
                if (evt->getPayloadSize() < payload_length) {
                    // Buffer is too small so expand it.
                    // Don't copy any data when expanding.
                    evt->getPayloadBuffer()->limit(0);
                    // Expand memory
                    evt->getPayloadBuffer()->expand(payload_length);
                }
                readData(evt->getPayloadBuffer(), payload_length);

                // evt->setRecordNumber(rcn);
                evt->setPayloadDataLength(payload_length);
                evt->setRecordNumber(record_number);
                evt->setStreamId(streamId);

                // Collect statistics
                missed_record = missed_record + (record_number - (prev_rec_number + 1));
                prev_rec_number = record_number;
                totalData = totalData + (double) total_length / 1000.0;
                rate++;

            } catch (std::runtime_error &e) {
                std::cout << e.what() << std::endl;
            }
        }

        void run() {

            try {
                auto start = std::chrono::high_resolution_clock::now();

                while (true) {
                    // Get an empty item from ring
                    auto buf = get();

                    decodeVtpHeaderCT(buf); //CT suggestion

                    // Make the buffer available for consumers
                    publish();

                    // Stat printing
                    auto end = std::chrono::high_resolution_clock::now();
                    std::chrono::duration<double, std::milli> elapsed = end - start;
                    if (elapsed.count() > 4000) {
                        printRates();
                        start = end;
                    }
                }

            } catch (std::runtime_error &e) {
                std::cout << e.what() << std::endl;
            }
        }


        void printRates()  {
            if (statLoop <= 0) {
                std::cout << "stream:" << streamId
                          << " event rate =" << std::to_string( rate / statPeriod )
                          << " Hz.  data rate =" << ( totalData / statPeriod ) << " kB/s."
                          << " missed rate = " << ( missed_record / statPeriod ) << " Hz." << std::endl;
                statLoop = statPeriod;
                rate = 0;
                totalData = 0;
                missed_record = 0;
            }
            statLoop--;
        }

    };

}

#endif  // ERSAP_VTP_RECEIVER_H
