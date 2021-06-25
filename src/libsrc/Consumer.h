
#ifndef ERSAP_VTP_CONSUMER_H
#define ERSAP_VTP_CONSUMER_H


#include <string>
#include <stdexcept>
#include <thread>
#include <iostream>
#include <chrono>
#include <functional>

#include "ByteBuffer.h"
#include "RingEvent.h"
#include "EUtil.h"
#include "Disruptor/Disruptor.h"

#include <boost/thread.hpp>
#include <boost/asio.hpp>

//#include <boost/asio/thread_pool.hpp>
//#include <boost/asio/post.hpp>

using namespace Disruptor;


namespace ersap {

    class Consumer {

    private:

        std::shared_ptr<RingBuffer<std::shared_ptr<RingEvent>>> ringBuffer;
        std::shared_ptr<ISequence> sequence;
        std::shared_ptr<ISequenceBarrier> barrier;

        long nextSequence;
        long availableSequence;

        /** Thread which does the producing. */
        boost::thread thd;

    public:


        /**
         * Consumer constructor
         */
        Consumer(std::shared_ptr<RingBuffer<std::shared_ptr<RingEvent>>> & ringBuffer,
                 std::shared_ptr<ISequence> & sequence,
                 std::shared_ptr<ISequenceBarrier> & barrier,
                 int runNumber) {

            this->ringBuffer = ringBuffer;
            this->sequence = sequence;
            this->barrier = barrier;

            //        ringBuffer->addGatingSequences(sequence);
            nextSequence = sequence->value() + 1L;
            availableSequence = -1L;
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
         *
         * @return next available item in ring buffer.
         * @throws InterruptedException
         */
        std::shared_ptr<RingEvent> get() {

            std::shared_ptr<RingEvent> item = nullptr;

            try {

                if (availableSequence < nextSequence) {
                    availableSequence = barrier->waitFor(nextSequence);
                }
                item = (*ringBuffer)[nextSequence];

            } catch (std::runtime_error &e) {
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
            sequence->setValue(nextSequence);

            // Go to next item to consume on input ring
            nextSequence++;
        }


        void run() {
            //        HitFinder hitFinder = new HitFinder();
            int threadCount = 8;

//            boost::asio::thread_pool pool(threadCount); // 4 threads
//            boost::asio::post(pool, [] {});
//            pool.join();

            // Create an asio::io_service and a thread_group (through pool in essence)
            boost::asio::io_service ioService;
            boost::thread_group threadpool;

            // This will start the ioService processing loop. All tasks
            // assigned with ioService.post() will start executing.
            boost::asio::io_service::work work(ioService);

            // This will add threads to the thread pool
            for (int i=0; i < threadCount; i++) {
                threadpool.create_thread(boost::bind(&boost::asio::io_service::run, &ioService));
            }

            while (true) {

                try {
                    // Get an empty item from ring
                    std::shared_ptr<RingEvent> buf = get();
                    if (buf->getPayloadSize() > 0) {
                        long frameTime = buf->getRecordNumber() * 65536L;
                        auto b = ByteBuffer::copyBuffer(buf->getPayloadBuffer());
                        put();
                        // This will assign tasks to the thread pool using lambda
                        ioService.post([frameTime, b] { return EUtil::decodePayloadMap3(frameTime, b); });
                    }
                }
                catch (std::runtime_error &e) {
                    std::cout << e.what() << std::endl;
                }
            }

            // This will stop the ioService processing loop
            ioService.stop();

            // Will wait till all the threads in the thread pool are finished with
            // their assigned tasks and 'join' them. Just assume the threads inside
            // the threadpool will be destroyed by this method.
            threadpool.join_all();

            // Or ...
            // pool.join();
        }


    };

}

#endif   // ERSAP_VTP_CONSUMER_H