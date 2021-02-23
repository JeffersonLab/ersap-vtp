

#ifndef ERSAP_VTP_TWOSTREAMAGG_H
#define ERSAP_VTP_TWOSTREAMAGG_H


#include <cstdlib>
#include <string>
#include <stdexcept>
#include <thread>
#include <chrono>

#include "ByteBuffer.h"
#include "RingEvent.h"
#include "Receiver.h"
#include "Consumer.h"
#include "Aggregator.h"

#include "Disruptor/Disruptor.h"
#include "Disruptor/SpinCountBackoffWaitStrategy.h"
#include "Disruptor/BlockingWaitStrategy.h"
#include "Disruptor/YieldingWaitStrategy.h"
#include <boost/thread.hpp>


using namespace Disruptor;


namespace ersap {


    class TwoStreamAggregator {

    private:

        /** VTP ports */
        int vtpPort1;
        int vtpPort2;

        /** Max ring items */
        static constexpr int maxRingItems = 32;

        /** Ring buffers */
        std::shared_ptr<RingBuffer<std::shared_ptr<RingEvent>>> ringBuffer1;
        std::shared_ptr<RingBuffer<std::shared_ptr<RingEvent>>> ringBuffer2;
        std::shared_ptr<RingBuffer<std::shared_ptr<RingEvent>>> ringBuffer12;

        /** Sequences */
        std::shared_ptr<ISequence> sequence1;
        std::shared_ptr<ISequence> sequence2;
        std::shared_ptr<ISequence> sequence12;

        /** Sequence barriers */
        std::shared_ptr<ISequenceBarrier> sequenceBarrier1;
        std::shared_ptr<ISequenceBarrier> sequenceBarrier2;
        std::shared_ptr<ISequenceBarrier> sequenceBarrier12;

    public:

        TwoStreamAggregator(int vtpPort1, int vtpPort2);
        void go();

    };
}

#endif  // ERSAP_VTP_TWOSTREAMAGG_H