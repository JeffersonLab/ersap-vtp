

#ifndef ERSAP_VTP_FAKE_H
#define ERSAP_VTP_FAKE_H


#include <string>
#include <stdexcept>
#include <iostream>
#include <chrono>

#include <boost/asio.hpp>


using namespace boost::asio;
using ip::tcp;

namespace ersap {

    /**
     * Sends fake VTP stream frames to a Receiver (server).
     */
    class FakeVtp {

    private:

        /** Stream ID */
        int streamId;

        /** Buffer size. */
        static constexpr size_t bufSize = 60;

        /** Array associated with buffer. */
        char buffie[bufSize];


    public:

        FakeVtp(int vtpPort, int streamId);

    };
}




#endif  // ERSAP_VTP_RECEIVER_H
