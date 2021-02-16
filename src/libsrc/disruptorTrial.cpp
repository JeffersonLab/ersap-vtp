//
// Created by Carl Timmer on 2/8/21.
//

#include "disruptorTrial.h"


namespace ersap {



    VardanERSAP::VardanERSAP() {

        //-----------------------
        // INPUT
        //-----------------------

        uint32_t spinTries = 10000;

        // Reserve room in vectors
        //crateSequences.reserve(streamCount);
        //crateBarriers.reserve(streamCount);

        // For each stream ...
        for (int i = 0; i < streamCount; i++) {
            // You may substitute a different wait strategy. This is one I created and is not
            // part of the original Disruptor distribution.
            //
            // Create a ring w/SpinCountBackoffWaitStrategy
            auto blockingStrategy = std::make_shared<BlockingWaitStrategy>();
            auto waitStrategy = std::make_shared<SpinCountBackoffWaitStrategy>(spinTries, blockingStrategy);
            crateRingBuffers[i] = RingBuffer<std::shared_ptr<RingEvent>>::createSingleProducer(
                    RingEvent::eventFactory(),
                    crateRingItemCount,
                    waitStrategy);

            // Create a sequence
            //auto const & seq = std::make_shared<Sequence>(Sequence::InitialCursorValue);
            crateSequences[i] = std::make_shared<Sequence>(Sequence::InitialCursorValue);

            // Create a barrier in the ring
            crateBarriers[i] = crateRingBuffers[i]->newBarrier();

            // Tell ring that after this sequence is "put back" by the consumer,
            // its associated ring item (ByteBuffer) will be
            // available for the producer to reuse (i.e. it's the last or gating consumer).

            // But put Sequence into vector first since C++ API requires it
            std::vector<std::shared_ptr<ISequence>> seqs;
            seqs.push_back(crateSequences[i]);
            crateRingBuffers[i]->addGatingSequences(seqs);

            // What sequence ring item do we want to get next?
//crateSequences[i]->addAndGet(1L);
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
                                                                                        outRingSize,
                                                                                        waitStrategy);
        outputSequence = std::make_shared<Sequence>(Sequence::InitialCursorValue);
        outputBarrier = outputRingBuffer->newBarrier();
        std::vector<std::shared_ptr<ISequence>> seqs;
        seqs.push_back(outputSequence);
        outputRingBuffer->addGatingSequences(seqs);
        outputNextSequence = outputSequence->value() + 1L;
    }


    /**
        * Run a setup with 2 crate producer threads, one crate consumer thread and one output ring
        * consumer thread.
        */
    void VardanERSAP::run() {

        try {
            // Create 2 producers
std::cout << "create 2 producers" << std::endl;
            CrateProducer producer1(this, 0);
            CrateProducer producer2(this, 1);

            // Create one crate consumer
std::cout << "create 1 crate consumer" << std::endl;
            CrateAggregatingConsumer crateConsumer(this, streamCount);

            // Create one output ring consumer
std::cout << "create 1 output consumer" << std::endl;
            OutputRingConsumer outputConsumer(this);

            // Now get all these threads running
std::cout << "start consumer & producer threads" << std::endl;
            outputConsumer.startThread();
            //crateConsumer.startThread();
            producer1.startThread();
            producer2.startThread();
std::cout << "done starting those threads" << std::endl;

            std::this_thread::sleep_for(std::chrono::seconds(4000));
std::cout << "done with sleep" << std::endl;

        } catch (std::runtime_error &e) {
            std::cout << e.what() << std::endl;
        }
    }


    /** Create and start a thread to execute the run() method of this class. */
    void VardanERSAP::startThread() {
        std::cout << "start main thread" << std::endl;
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

