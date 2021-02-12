
#ifndef ERSAP_VTP_AGGREGATOR_H
#define ERSAP_VTP_AGGREGATOR_H


#include <cstdlib>
#include <string>
#include <cstring>
#include <stdexcept>
#include <thread>
#include <unordered_map>
#include <utility>

#include "ByteBuffer.h"
#include "RingEvent.h"
#include "Disruptor/Disruptor.h"
#include "Disruptor/SpinCountBackoffWaitStrategy.h"
#include "Disruptor/BlockingWaitStrategy.h"
#include "Disruptor/YieldingWaitStrategy.h"
#include <boost/thread.hpp>


using namespace Disruptor;


namespace ersap {


    /**
     * __
     * /  \
     * \  /     ___       __
     * --  --> |   |     /  \
     * __      |   |-->  \  /
     * /  \ --> ---       --
     * \  /
     * --
     */
    class Aggregator {

    private:

        /** Maps for aggregation  */
        std::unordered_map<long, uint8_t *> m1;
        std::unordered_map<long, uint8_t *> m2;

        /** Current spot in output ring from which an item was claimed. */
        int64_t getOutSequence;

        /** 1 RingBuffer per stream. */
        std::shared_ptr<RingBuffer<std::shared_ptr<RingEvent>>> ringBuffer1;
        std::shared_ptr<RingBuffer<std::shared_ptr<RingEvent>>> ringBuffer2;

        /** 1 sequence per stream */
        std::shared_ptr<ISequence> sequence1;
        std::shared_ptr<ISequence> sequence2;

        /** 1 barrier per stream  */
        std::shared_ptr<ISequenceBarrier> barrier1;
        std::shared_ptr<ISequenceBarrier> barrier2;

        /** Track which sequence the aggregating consumer wants next from each of the crate rings. */
        long nextSequence1;
        long nextSequence2;

        /** Track which sequence is currently available from each of the crate rings. */
        long availableSequence1;
        long availableSequence2;

        /** 1 output RingBuffer. */
        std::shared_ptr<RingBuffer<std::shared_ptr<RingEvent>>> outputRingBuffer;

        /** Thread which runs everything. */
        boost::thread thd;


    public:

        Aggregator(std::shared_ptr<RingBuffer<std::shared_ptr<RingEvent>>> ringBuffer1,
                   std::shared_ptr<RingBuffer<std::shared_ptr<RingEvent>>> ringBuffer2,
                   std::shared_ptr<ISequence> sequence1,
                   std::shared_ptr<ISequence> sequence2,
                   std::shared_ptr<ISequenceBarrier> barrier1,
                   std::shared_ptr<ISequenceBarrier> barrier2,
                   std::shared_ptr<RingBuffer<std::shared_ptr<RingEvent>>> outputRingBuffer) {

            this->ringBuffer1 = ringBuffer1;
            this->ringBuffer2 = ringBuffer2;
            this->sequence1 = sequence1;
            this->sequence2 = sequence2;
            this->barrier1 = barrier1;
            this->barrier2 = barrier2;
            this->outputRingBuffer = outputRingBuffer;

//            std::vector<std::shared_ptr<Disruptor::ISequence>> writeSeqs;
//            writeSeqs.push_back(sequence1);
//            ringBuffer1->addGatingSequences(writeSeqs);
//
//            std::vector<std::shared_ptr<Disruptor::ISequence>> writeSeqs2;
//            writeSeqs2.push_back(sequence2);
//            ringBuffer2->addGatingSequences(writeSeqs2);

            nextSequence1 = sequence1->value() + 1L;
            nextSequence2 = sequence2->value() + 1L;

            availableSequence1 = -1L;
            availableSequence2 = -1L;
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


        void get() {

                try {
                    if (availableSequence1 < nextSequence1) {
                        availableSequence1 = barrier1->waitFor(nextSequence1);
                    }
                    auto inputItem1 = (*ringBuffer1)[(nextSequence1)];

                    if (availableSequence2 < nextSequence2) {
                        availableSequence2 = barrier2->waitFor(nextSequence2);
                    }
                    auto inputItem2 = (*ringBuffer2)[(nextSequence2)];

                    long b1 = inputItem1->getRecordNumber();
                    long b2 = inputItem2->getRecordNumber();

                    int l1 = inputItem1->getPayloadDataLength();
                    int l2 = inputItem2->getPayloadDataLength();

                    m1.insert({b1, inputItem1->getPayload()});
                    m1.insert({b2, inputItem2->getPayload()});

                    int64_t aggRecNum = -1;

                    getOutSequence = outputRingBuffer->next();
                    auto outputItem = (*outputRingBuffer)[getOutSequence];

                    outputItem->getPayloadBuffer()->clear();

                    if (outputItem->getPayloadSize() < (l1 + l2)) {
                        outputItem->increaseSize((l1 + l2));
                        outputItem->setPayloadDataLength(l1 + l2);
                    }

                    if ((m1.count(b1) > 0) && (m2.count(b1) > 0)) {
                        addByteArrays(m1[b1], l1, m2[b1], l2, outputItem->getPayload());
                        aggRecNum = b1;
                        m1.erase(b1);
                        m2.erase(b1);
                    }

                    if ((m1.count(b2) > 0) && (m2.count(b2) > 0)) {
                        addByteArrays(m1[b2], l1, m2[b2], l2, outputItem->getPayload());
                        aggRecNum = b2;
                        m1.erase(b2);
                        m2.erase(b2);
                    }

                    if (aggRecNum > -1) {
                        outputItem->setRecordNumber(aggRecNum);
                        outputItem->setPayloadDataLength(l1 + l2);
                    }

                }
                catch (std::runtime_error & e) {
                    std::cout << e.what() << std::endl;
                }
        }


    private:

        void addByteArrays(uint8_t * a, int aLength, uint8_t *  b, int bLength, uint8_t * c) {
            std::memcpy(c, a, aLength);
            std::memcpy(c + aLength, b, bLength);
        }


        /**
         * This "consumer" is also a producer for the output ring.
         * So get items from the output ring and fill them with items claimed from the input rings.
         */
        void put() {

            outputRingBuffer->publish(getOutSequence);

            sequence1->setValue(nextSequence1);
            nextSequence1++;

            sequence2->setValue(nextSequence2);
            nextSequence2++;
        }

    public:

        void run() {
            try {
                while (true) {
                    get();
                    put();
                }

            } catch (std::runtime_error & e) {
                std::cout << e.what() << std::endl;
            }
        }

    };

}

#endif // ERSAP_VTP_AGGREGATOR_H