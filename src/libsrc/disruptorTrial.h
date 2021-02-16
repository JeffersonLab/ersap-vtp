//
// Created by Carl Timmer on 2/8/21.
//

#ifndef ERSAP_VTP_DISRUPTORTRIAL_H
#define ERSAP_VTP_DISRUPTORTRIAL_H

#include <string>
#include <stdexcept>
#include <thread>
#include <vector>

#include "ByteBuffer.h"
#include "RingEvent.h"
#include "Disruptor/Disruptor.h"
#include "Disruptor/SpinCountBackoffWaitStrategy.h"
#include "Disruptor/BlockingWaitStrategy.h"
#include <boost/thread.hpp>
#include <boost/chrono.hpp>


using namespace Disruptor;


namespace ersap {

    /**
     * This class is an example of how one might take 2 producers (one for each ring)
     * and have a consumer that reads one item from each ring and puts them both into
     * a third, output ring. That output ring has a consumer that looks at each item
     * in the output ring.
     *
     *
     * @author Carl Timmer
     */
    class VardanERSAP {

    public:

        /** Number of streams in 1 crate. */
        static constexpr int streamCount = 2;

        /** Size of ByteBuffers in ring. */
        static constexpr int byteBufferSize = 2048;

        /** Number of items in each ring buffer. Must be power of 2. */
        static constexpr uint32_t crateRingItemCount = 32;

        /** 1 RingBuffer per stream. */
        std::shared_ptr<RingBuffer<std::shared_ptr<RingEvent>>> crateRingBuffers[streamCount];

        /** 1 sequence per stream */
        std::shared_ptr<ISequence> crateSequences[streamCount];

        /** 1 barrier per stream */
        std::shared_ptr<ISequenceBarrier> crateBarriers[streamCount];

        /** Track which sequence the aggregating consumer wants next from each of the crate rings. */
        int64_t crateNextSequences[streamCount];

        /** Track which sequence is currently available from each of the crate rings. */
        int64_t crateAvailableSequences[streamCount];


        // OUTPUT RING FOR AGGREGATING CONSUMER

        /** 1 output RingBuffer. */
        std::shared_ptr<RingBuffer<std::shared_ptr<RingEvent>>> outputRingBuffer = nullptr;

        /** Number of RingEvents held in outputput ring. Must be power of 2 and &gt;= total
         *  number of RingEvents in all crateRingBuffers. */
        static constexpr int outRingSize = 2*crateRingItemCount;

        /** 1 sequence for the output ring's consumer */
        std::shared_ptr<ISequence> outputSequence;

        /** 1 barrier for output ring's consumer */
        std::shared_ptr<ISequenceBarrier> outputBarrier;

        /** Track which sequence the output consumer wants next from output ring. */
        int64_t outputNextSequence;

        /** Track which sequence is currently available from the output ring. */
        int64_t outputAvailableSequence = -1;


        // Threads

        /** Thread which runs everything. */
        boost::thread thd;


        // Classes

        /** Thread to produce one stream in one crate. */
        class CrateProducer {

        private:

            /** Object which created this thread. */
            VardanERSAP *owner;
            /** Is this stream 0 or stream 1? */
            int streamNum;
            /** Current spot in ring from which an item was claimed. */
            int64_t getSequence = -1L;
            /** Thread which does the producing. */
            boost::thread thd;

        public:

            CrateProducer(VardanERSAP *pOwner, int streamNumber) {
                owner = pOwner;
                streamNum = streamNumber;
            }

            /** Create and start a thread to execute the run() method of this class. */
            void startThread() {
std::cout << "start producer thread " << streamNum << std::endl;
                thd = boost::thread([this]() { this->run(); });
            }

            /** Stop the thread. */
            void stopThread() {
                // Send signal to interrupt it
                thd.interrupt();
                // Wait for it to stop
                thd.join();
            }

            /**
            * Get the next available item in ring buffer for writing/reading data.
            * @return next available item in ring buffer.
            */
            std::shared_ptr<RingEvent> get() {
                // Next available item for producer
                getSequence = owner->crateRingBuffers[streamNum]->next();
                std::cout << "producer " << streamNum << ": get " << getSequence << std::endl;

                // Get object in that position (sequence) of ring
                std::shared_ptr<RingEvent> &item = (*(owner->crateRingBuffers[streamNum]))[getSequence];
                return item;
            }


            /**
             * Used to tell the consumer that the ring buffer item gotten with this producer's
             * last call to {@link #get()} (and all previously gotten items) is ready for consumption.
             * To be used in after {@link #get()}.
             */
            void publish() {
                std::cout << "producer " << streamNum << ": publish " << getSequence << std::endl;
                owner->crateRingBuffers[streamNum]->publish(getSequence);
            }


            void run() {
                try {
                    while (true) {
                        // Get an empty item from ring
                        auto item = get();

                        // Do something with item here, like write data into it ...

                        // Make the buffer available for consumers
                        publish();
                    }
                }
                catch (std::runtime_error &e) {
                    std::cout << e.what() << std::endl;
                }
            }
        };


        /** Thread to consume from two streams in one crate and send (be a producer for) an output ring. */
        class CrateAggregatingConsumer {

            /** Array to store items obtained from both the crate (input) rings. */
            std::vector<std::shared_ptr<RingEvent>> inputItems;

            /** Array to store items obtained from both the output ring. */
            std::vector<std::shared_ptr<RingEvent>> outputItems;

            /** Current spot in output ring from which an item was claimed. */
            int64_t getOutSequence = -1L;

            /** Object which created this thread. */
            VardanERSAP *owner = nullptr;

            /** How many streams are coming together here. */
            int streamCount;

            /** Thread which does the producing. */
            boost::thread thd;

        public:

            CrateAggregatingConsumer(VardanERSAP *pOwner, int strmCnt) {
                owner = pOwner;
                streamCount = strmCnt;
                inputItems.reserve(streamCount);
                outputItems.reserve(streamCount);
            }


            /** Create and start a thread to execute the run() method of this class. */
            void startThread() {
                std::cout << "start crate consumer thread" << std::endl;
                thd = boost::thread([this]() { this->run(); });
            }

            /** Stop the thread. */
            void stopThread() {
                // Send signal to interrupt it
                thd.interrupt();
                // Wait for it to stop
                thd.join();
            }

            /**
             * Get the next available item from each crate ring buffer.
             * Do NOT call this multiple times in a row!
             * Be sure to call "put" before calling this again.
             * @return next available item in ring buffer for getting data already written into.
             * @throws InterruptedException
             */
            void get() {

                try {
                    // Grab one ring item from each ring ...

                    for (int i = 0; i < streamCount; i++) {
                        // Only wait for read-volatile-memory if necessary ...
                        if (owner->crateAvailableSequences[i] < owner->crateNextSequences[i]) {
                            // Note: the returned (available) sequence may be much larger than crateNextSequence[i]
                            // which means in the next iteration, we do NOT have to wait here.
                            owner->crateAvailableSequences[i] = owner->crateBarriers[i]->waitFor(
                                    owner->crateNextSequences[i]);
                        }

                        inputItems[i] = (*owner->crateRingBuffers[i])[owner->crateNextSequences[i]];

                        // Get next available slot in output ring (as producer)
                        getOutSequence = owner->outputRingBuffer->next();

                        // Get object in that position (sequence or slot) of output ring
                        outputItems[i] = (*owner->outputRingBuffer)[getOutSequence];
                    }
                }
                catch (Disruptor::TimeoutException & e) {
                    // never happen since we don't use timeout wait strategy
                }
                catch (Disruptor::AlertException & e) {
                    std::cout << e.what() << std::endl;
                }
            }


            /**
             * This "consumer" is also a producer for the output ring.
             * So get items from the output ring and fill them with items claimed from the input rings.
             */
            void put() {

                // Tell output ring, we're done with all items we took from it.
                // Make them available to output ring's consumer.
                //
                // By releasing getOutputSequence, we release that item and all
                // previously obtained items, so we only have to call this once
                // with the last sequence.
                owner->outputRingBuffer->publish(getOutSequence);

                for (int i = 0; i < streamCount; i++) {
                    // Tell input (crate) ring that we're done with the item we're consuming
                    owner->crateSequences[i]->setValue(owner->crateNextSequences[i]);

                    // Go to next item to consume from input ring
                    owner->crateNextSequences[i]++;
                }
            }


            void run() {
                try {
                    while (true) {
                        // Get one item from each of a single crate's rings
                        std::cout << "crate consumer: get one item from each ring" << std::endl;
                        get();

                        // Do something with buffers here, like write data into them.
                        // Buffers are in "inputItems" and "outputItems" arrays.
                        // Copy data from crate ring item to output ring item, or do something else .....
                        for (int i = 0; i < streamCount; i++) {
                            // The assignment operator of RingEvent COPIES the data
                            outputItems[i] = inputItems[i];
                        }

                        // Done with buffers so make them available for all rings again for reuse
                        std::cout << "crate consumer: put copied items into ring" << std::endl;
                        put();
                    }

                } catch (std::runtime_error &e) {
                    std::cout << e.what() << std::endl;
                }
            }
        };


        /** Thread to consume from output ring. */
        class OutputRingConsumer {

            /** Object which created this thread. */
            VardanERSAP *owner = nullptr;

            /** Current spot in output ring from which an item was claimed. */
            int64_t getOutSequence = 0;

            /** Thread which does the producing. */
            boost::thread thd;


        public:


            OutputRingConsumer(VardanERSAP *pOwner) {
                owner = pOwner;
            }


            /** Create and start a thread to execute the run() method of this class. */
            void startThread() {
                std::cout << "start output ring consumer thread" << std::endl;
                thd = boost::thread([this]() { this->run(); });
            }

            /** Stop the thread. */
            void stopThread() {
                // Send signal to interrupt it
                thd.interrupt();
                // Wait for it to stop
                thd.join();
            }


            /**
               * Get the next available item from outupt ring buffer.
               * Do NOT call this multiple times in a row!
               * Be sure to call "put" before calling this again.
               * @return next available item in ring buffer.
               * @throws InterruptedException
               */
            std::shared_ptr<RingEvent> get() {

                std::shared_ptr<RingEvent> item = nullptr;

                try {
                    if (owner->outputAvailableSequence < owner->outputNextSequence) {
                        owner->outputAvailableSequence = owner->outputBarrier->waitFor(owner->outputNextSequence);
                    }

                    item = (*owner->outputRingBuffer)[owner->outputNextSequence];
                }
                catch (Disruptor::TimeoutException & e) {
                    // never happen since we don't use timeout wait strategy
                }
                catch (Disruptor::AlertException & e) {
                    std::cout << e.what() << std::endl;
                }

                return item;
            }


            /**
             * This "consumer" is also a producer for the output ring.
             * So get items from the output ring and fill them with items claimed from the input rings.
             */
            void put() {

                // Tell input (crate) ring that we're done with the item we're consuming
                owner->outputSequence->setValue(owner->outputNextSequence);

                // Go to next item to consume on input ring
                owner->outputNextSequence++;
            }


            void run() {
                try {
                    while (true) {
                        std::cout << "output ring consumer: get empty item from ring" << std::endl;
                        // Get an empty item from ring
                        std::shared_ptr<RingEvent> item = get();

                        // Do something with item here, like write data into it ...

                        // Make the buffer available for consumers
                        std::cout << "output ring consumer: put item back into ring" << std::endl;
                        put();
                    }

                } catch (std::runtime_error &e) {
                    std::cout << e.what() << std::endl;
                }
            }

        };





    public:

        VardanERSAP();

        void run();
        void startThread();
        void stopThread();

    };

}

#endif //ERSAP_VTP_DISRUPTORTRIAL_H
