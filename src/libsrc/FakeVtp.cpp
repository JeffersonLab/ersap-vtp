


#include "FakeVtp.h"


namespace ersap {


    FakeVtp::FakeVtp(int vtpPort, int streamId) {

            this->streamId = streamId;

            mutable_buffers_1 buffer((void *) buffie, bufSize);

            try {
                std::string host = "localhost";
                std::string port = std::to_string(vtpPort);

                boost::asio::io_service io_service;

                tcp::resolver resolver(io_service);
                tcp::resolver::query query(tcp::v4(), host, port);
                tcp::resolver::iterator iterator = resolver.resolve(query);

                tcp::socket sock(io_service);
                boost::asio::connect(sock, iterator);


                // send 2 ints
                int firstInts[2] = {111,222};
                auto first = mutable_buffers_1((void *) firstInts, sizeof(firstInts));
                boost::asio::write(sock, first);


                // Send buffer of 52 bytes in a loop
                char *data = buffie;
                char *pRecNum, *pSec, *pNsec, *pData = data;

                int32_t source_id = streamId;
                int32_t total_length = 60;
                int32_t payload_length = 8;
                int32_t compressed_length = 60;
                int32_t magic = 0xc0da0100;
                int32_t format_version = 6;
                int32_t flags = 7;

                int64_t record_number = 1L;
                int64_t ts_sec = 1L;
                int64_t ts_nsec = 1L;
                int64_t longData = 123456789L;

                std::memcpy(pData, &source_id, 4); pData += 4;
                std::memcpy(pData, &total_length, 4); pData += 4;
                std::memcpy(pData, &payload_length, 4); pData += 4;
                std::memcpy(pData, &compressed_length, 4); pData += 4;
                std::memcpy(pData, &magic, 4); pData += 4;
                std::memcpy(pData, &format_version, 4); pData += 4;
                std::memcpy(pData, &flags, 4); pData += 4;

                pRecNum = pData;
                std::memcpy(pData, &record_number, 8); pData += 8;
                pSec = pData;
                std::memcpy(pData, &ts_sec, 8); pData += 8;
                pNsec = pData;
                std::memcpy(pData, &ts_nsec, 8); pData += 8;
                std::memcpy(pData, &longData, 8);

                while (true) {
                    boost::asio::write(sock, buffer, boost::asio::transfer_exactly(bufSize));

                    // change record # and time stamp
                    record_number++;
                    ts_sec++;
                    ts_nsec++;

                    // start over if necessary
                    if (record_number < 0) {
                        record_number = ts_sec = ts_nsec = 0L;
                    }

                    std::memcpy(pRecNum, &record_number, 8);
                    std::memcpy(pSec, &ts_sec, 8);
                    std::memcpy(pNsec, &ts_nsec, 8);
                }

            }
            catch (std::exception& e) {
                std::cerr << "Exception: " << e.what() << "\n";
            }

        }


}


int main(int argc, char **argv) {

    char* p_end;

    int port = 45100, streamId = 1;
    std::cout << "argc = " << argc << std::endl;

    if (argc > 1) {
        port = (int) strtol(argv[1], &p_end, 10);
    }

    if (argc > 2) {
        streamId = (int) strtol(argv[2], &p_end, 10);
    }

    std::cout << "port = " << port << ", stream id = " << streamId << std::endl;

    ersap::FakeVtp sender(port, streamId);

    std::cout << "IN main, send data ---->" << std::endl;

    std::this_thread::sleep_for(std::chrono::seconds(4000));
    return 0;
}

