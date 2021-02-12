

#include "TwoStreamAggregator.h"


namespace ersap {


    TwoStreamAggregator::TwoStreamAggregator(int vtpPort1, int vtpPort2) {
            this->vtpPort1 = vtpPort1;
            this->vtpPort2 = vtpPort2;

            // Try different wait strategies

//            uint32_t spinTries = 10000;
//            auto blockingStrategy = std::make_shared<Disruptor::BlockingWaitStrategy>();
//            auto waitStrategy = std::make_shared<Disruptor::SpinCountBackoffWaitStrategy>(spinTries, blockingStrategy);


            auto waitStrategy = std::make_shared<Disruptor::YieldingWaitStrategy>();
            ringBuffer1 = RingBuffer<std::shared_ptr<RingEvent>>::createSingleProducer(
                             RingEvent::eventFactory(), maxRingItems,  waitStrategy);
            sequence1 = std::make_shared<Disruptor::Sequence>(Disruptor::Sequence::InitialCursorValue);
            sequenceBarrier1 = ringBuffer1->newBarrier();
            std::vector<std::shared_ptr<Disruptor::ISequence>> writeSeqs1;
            writeSeqs1.push_back(sequence1);
            ringBuffer1->addGatingSequences(writeSeqs1);


            auto waitStrategy2 = std::make_shared<Disruptor::YieldingWaitStrategy>();
            ringBuffer2 = RingBuffer<std::shared_ptr<RingEvent>>::createSingleProducer(
                              RingEvent::eventFactory(), maxRingItems,  waitStrategy2);
            sequence2 = std::make_shared<Disruptor::Sequence>(Disruptor::Sequence::InitialCursorValue);
            sequenceBarrier2 = ringBuffer2->newBarrier();
            std::vector<std::shared_ptr<Disruptor::ISequence>> writeSeqs2;
            writeSeqs2.push_back(sequence2);
            ringBuffer2->addGatingSequences(writeSeqs2);


            auto waitStrategy12 = std::make_shared<Disruptor::YieldingWaitStrategy>();
            ringBuffer12 = RingBuffer<std::shared_ptr<RingEvent>>::createSingleProducer(
                               RingEvent::eventFactory(), maxRingItems,  waitStrategy12);
            sequence12 = std::make_shared<Disruptor::Sequence>(Disruptor::Sequence::InitialCursorValue);
            sequenceBarrier12 = ringBuffer12->newBarrier();
            std::vector<std::shared_ptr<Disruptor::ISequence>> writeSeqs12;
            writeSeqs12.push_back(sequence12);
            ringBuffer12->addGatingSequences(writeSeqs12);
        }

        void TwoStreamAggregator::go() {
            Receiver receiver1(vtpPort1, 1, ringBuffer1, 10);
            Receiver receiver2(vtpPort2, 2, ringBuffer2, 10);

            Aggregator aggregator12(ringBuffer1, ringBuffer2, sequence1,
                                    sequence2, sequenceBarrier1, sequenceBarrier2,
                                    ringBuffer12);

            int runNumber = 0;
            Consumer consumer(ringBuffer12, sequence12, sequenceBarrier12, runNumber);

            receiver1.startThread();
            receiver2.startThread();

            aggregator12.startThread();
            consumer.startThread();
        }

}

int main(int argc, char **argv) {
    char* p_end;

    int port1 = (int)strtol(argv[0], &p_end, 10);
    int port2 = (int)strtol(argv[1], &p_end, 10);

    ersap::TwoStreamAggregator ag(port1, port2);
    ag.go();
    return 0;
}
