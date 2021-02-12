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
        static constexpr uint32_t crateRingItemCount = 256;

        /** 1 RingBuffer per stream. */
        std::shared_ptr<RingBuffer<std::shared_ptr<RingEvent>>> crateRingBuffers[streamCount];

        /** 1 sequence per stream */
        std::vector<std::shared_ptr<ISequence>> crateSequences;

        /** 1 barrier per stream */
        std::vector<std::shared_ptr<ISequenceBarrier>> crateBarriers;

        /** Track which sequence the aggregating consumer wants next from each of the crate rings. */
        int64_t crateNextSequences[streamCount];

        /** Track which sequence is currently available from each of the crate rings. */
        int64_t crateAvailableSequences[streamCount];


        // OUTPUT RING FOR AGGREGATING CONSUMER

        /** 1 output RingBuffer. */
        std::shared_ptr<RingBuffer<std::shared_ptr<RingEvent>>> outputRingBuffer = nullptr;

        /** Number of RingEvents held in outputput ring. Must be power of 2. */
        int outRingSize = 64;

        /** 1 sequence for the output ring's consumer */
        std::shared_ptr<ISequence> outputSequence;

        /** 1 barrier for output ring's consumer */
        std::shared_ptr<ISequenceBarrier> outputBarrier;

        /** Track which sequence the output consumer wants next from output ring. */
        int64_t outputNextSequence;

        /** Track which sequence is currently available from the output ring. */
        int64_t outputAvailableSequence = -1;


        // Threads

        /** Thread which does the file writing. */
        boost::thread CrateProducerThd;

        /** Thread to consume from two streams in one crate and send (be a producer for) an output ring. */
        boost::thread CrateAggregatingThd;

        /** Thread to consume from output ring. */
        boost::thread OutputConsumerThd;

        /** Thread which runs everything. */
        boost::thread thd;


    public:

        VardanERSAP();

        void run();
        void startThread();
        void stopThread();

    };

}

#endif //ERSAP_VTP_DISRUPTORTRIAL_H
