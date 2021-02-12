//
// Created by Carl Timmer on 2/8/21.
//

#include "disruptorTrial.h"


namespace ersap {


    /** Thread to produce one stream in one crate. */
    class CrateProducer {

    private:

        /** Object which created this thread. */
        VardanERSAP *owner = nullptr;
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

            // Get object in that position (sequence) of ring
            std::shared_ptr<RingEvent> &item = (*owner->crateRingBuffers[streamNum])[getSequence];
            return item;
        }


        /**
         * Used to tell the consumer that the ring buffer item gotten with this producer's
         * last call to {@link #get()} (and all previously gotten items) is ready for consumption.
         * To be used in after {@link #get()}.
         */
        void publish() {
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

                    inputItems[i] = (*owner->crateRingBuffers[i])[(owner->crateNextSequences[i])];

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
                    get();

                    // Do something with buffers here, like write data into them.
                    // Buffers are in "inputItems" and "outputItems" arrays.
                    // Copy data from crate ring item to output ring item, or do something else .....
                    for (int i = 0; i < streamCount; i++) {
                        // The assignment operator of RingEvent COPIES the data
                        outputItems[i] = inputItems[i];
                    }

                    // Done with buffers so make them available for all rings again for reuse
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
                    // Get an empty item from ring
                    std::shared_ptr<RingEvent> item = get();

                    // Do something with item here, like write data into it ...

                    // Make the buffer available for consumers
                    put();
                }

            } catch (std::runtime_error &e) {
                std::cout << e.what() << std::endl;
            }
        }

    };




    VardanERSAP::VardanERSAP() {

        outputRingBuffer = RingBuffer<std::shared_ptr<RingEvent>>::createSingleProducer(RingEvent::eventFactory(),
                                                                                        crateRingItemCount);

        // You may substitute a different wait strategy. This is one I created and is not
        // part of the original Disruptor distribution.

        //-----------------------
        // INPUT
        //-----------------------

        uint32_t spinTries = 10000;

        // Reserve room in vectors
        crateSequences.reserve(streamCount);
        crateBarriers.reserve(streamCount);

        // For each stream ...
        for (int i = 0; i < streamCount; i++) {
            // Create a ring w/SpinCountBackoffWaitStrategy
            auto blockingStrategy = std::make_shared<Disruptor::BlockingWaitStrategy>();
            auto waitStrategy = std::make_shared<Disruptor::SpinCountBackoffWaitStrategy>(spinTries, blockingStrategy);
            crateRingBuffers[i] = RingBuffer<std::shared_ptr<RingEvent>>::createSingleProducer(
                    RingEvent::eventFactory(),
                    crateRingItemCount,
                    waitStrategy);
            // Create a sequence
            crateSequences[i] = std::make_shared<Disruptor::Sequence>(Disruptor::Sequence::InitialCursorValue);

            // Create a barrier in the ring
            crateBarriers[i] = crateRingBuffers[i]->newBarrier();

            // Tell ring that after this sequence is "put back" by the consumer,
            // its associated ring item (ByteBuffer) will be
            // available for the producer to reuse (i.e. it's the last or gating consumer).

            // But put Sequence into vector first since C++ API requires it
            std::vector<std::shared_ptr<Disruptor::ISequence>> writeSeqs;
            writeSeqs.push_back(crateSequences[i]);
            crateRingBuffers[i]->addGatingSequences(writeSeqs);

            // What sequence ring item do we want to get next?
            crateSequences[i]->addAndGet(1L);
            crateNextSequences[i] = crateSequences[i]->value() + 1L;

            // Initialize these values to indicate nothing is currently available from the ring
            crateAvailableSequences[i] = -1L;
        }

        //-----------------------
        // OUTPUT
        //-----------------------

        // Now create output ring
        auto blockingStrategy = std::make_shared<Disruptor::BlockingWaitStrategy>();
        auto waitStrategy = std::make_shared<Disruptor::SpinCountBackoffWaitStrategy>(spinTries, blockingStrategy);
        outputRingBuffer = RingBuffer<std::shared_ptr<RingEvent>>::createSingleProducer(RingEvent::eventFactory(),
                                                                                        crateRingItemCount,
                                                                                        waitStrategy);
        outputSequence = std::make_shared<Disruptor::Sequence>(Disruptor::Sequence::InitialCursorValue);
        outputBarrier = outputRingBuffer->newBarrier();
        std::vector<std::shared_ptr<Disruptor::ISequence>> writeSeqs;
        writeSeqs.push_back(outputSequence);
        outputRingBuffer->addGatingSequences(writeSeqs);
        outputNextSequence = outputSequence->value() + 1L;
    }


    /**
        * Run a setup with 2 crate producer threads, one crate consumer thread and one output ring
        * consumer thread.
        */
    void VardanERSAP::run() {

        try {
            // Create 2 producers
            CrateProducer *producer1 = new CrateProducer(this, 0);
            CrateProducer *producer2 = new CrateProducer(this, 1);

            // Create one crate consumer
            CrateAggregatingConsumer *crateConsumer = new CrateAggregatingConsumer(this, streamCount);

            // Create one output ring consumer
            OutputRingConsumer *outputConsumer = new OutputRingConsumer(this);

            // Now get all these threads running
            outputConsumer->startThread();
            crateConsumer->startThread();
            producer1->startThread();
            producer2->startThread();

            std::this_thread::sleep_for(std::chrono::seconds(100));

        } catch (std::runtime_error &e) {
            std::cout << e.what() << std::endl;
        }
    }


    /** Create and start a thread to execute the run() method of this class. */
    void VardanERSAP::startThread() {
        thd = boost::thread([this]() { this->run(); });
    }

    /** Stop the thread. */
    void VardanERSAP::stopThread() {
        // Send signal to interrupt it
        thd.interrupt();
        // Wait for it to stop
        thd.join();
    }


}



int main(int argc, char **argv) {

    ersap::VardanERSAP test;

    std::cout << "IN main, start all threads" << std::endl;

    test.startThread();
    return 0;
}

