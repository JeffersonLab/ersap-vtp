# To use this, do the following the first time:
#   % mkdir build
#   % mkdir build/cmake
#   % cd build/cmake
#   % cmake ../..
#   % make (install)
#
#
# After doing this once can do the following to recompile
#  % cd <top level evio dir>
#  % cmake --build build/cmake (--target install)
#
#
# Set the path in which to install.
# This can be overridden on command line (in build/cmake dir) with:
#   % cmake -DCMAKE_INSTALL_PREFIX=<my_dir> ../..
#   % make install
# (This call must be placed BEFORE "project" command).
set(CMAKE_INSTALL_PREFIX ./)

cmake_minimum_required(VERSION 3.17)

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED True)
set(CMAKE_DEBUG_POSTFIX -dbg)

project(ersap_vtp VERSION 1.0)

# Place libs & binaries in build/cmake/lib and bin
#set(CMAKE_INSTALL_PREFIX   /Users/timmer/coda/codaIn1)
set(LIBRARY_OUTPUT_PATH    ${CMAKE_BINARY_DIR}/lib)
set(EXECUTABLE_OUTPUT_PATH ${CMAKE_BINARY_DIR}/bin)

message(STATUS "CMAKE_BINARY_DIR = " ${CMAKE_BINARY_DIR})
message(STATUS "CMAKE_INSTALL_PREFIX = " ${CMAKE_INSTALL_PREFIX})
message(STATUS "CMAKE_INSTALL_LIBDIR = " ${CMAKE_INSTALL_LIBDIR})
message(STATUS "CMAKE_INSTALL_INCLUDEDIR = " ${CMAKE_INSTALL_INCLUDEDIR})


set(CPP_HEADER_FILES
        src/libsrc/ByteOrder.h
        src/libsrc/ByteBuffer.h
        src/libsrc/EvioException.h
        src/libsrc/disruptorTrial.h
        src/libsrc/RingEvent.h
        src/libsrc/Receiver.h
        src/libsrc/Consumer.h
        src/libsrc/EUtil.h
        src/libsrc/ChargeTime.h
        src/libsrc/Aggregator.h
        src/libsrc/TwoStreamAggregator.h
        )

set(CPP_LIB_FILES
        src/libsrc/ByteOrder.cpp
        src/libsrc/ByteBuffer.cpp
        src/libsrc/disruptorTrial.cpp
        src/libsrc/RingEvent.cpp
        )


include(FindPackageHandleStandardArgs)

# search for boost libs
find_package(Boost REQUIRED system thread chrono)

if (Boost_FOUND)
    message(STATUS "Boost Found: libs = " ${Boost_LIBRARIES} ", include dirs = " ${Boost_INCLUDE_DIRS})
    link_directories(${Boost_LIBRARY_DIRS})
elseif(NOT Boost_FOUND)
    error("Boost Not Found")
endif()

# Remove from cache so new search done each time
unset(DISRUPTOR_INCLUDE_DIR CACHE)
unset(DISRUPTOR_LIBRARY CACHE)

# Search for disruptor includes & lib
find_path(DISRUPTOR_INCLUDE_DIR
        NAMES Disruptor.h
        PATHS $ENV{DISRUPTOR_CPP_HOME}/Disruptor
        NO_DEFAULT_PATH
        )

find_library(DISRUPTOR_LIBRARY
        NAMES Disruptor
        PATHS $ENV{DISRUPTOR_CPP_HOME}/build/Disruptor
        NO_DEFAULT_PATH
        )


if( DISRUPTOR_INCLUDE_DIR )
    message(STATUS "Disruptor.h found in path = " $ENV{DISRUPTOR_CPP_HOME}/Disruptor)
    # Reset this to one directory upstream since these files are included as
    # include "Disruptor/xxx.h
    set (DISRUPTOR_INCLUDE_DIR  $ENV{DISRUPTOR_CPP_HOME})
    message(STATUS "Disruptor include directory now set to " ${DISRUPTOR_INCLUDE_DIR})
else()
    message(FATAL_ERROR "Disruptor.h NOT found, cmake will exit." )
endif()


if( DISRUPTOR_LIBRARY )
    message(STATUS "Disruptor library found at = " ${DISRUPTOR_LIBRARY})
else()
    message(FATAL_ERROR "Disruptor library NOT found, cmake will exit." )
endif()


# Shared ersap C++ library
add_library(ersap-vtp SHARED ${CPP_LIB_FILES})
target_link_libraries(ersap-vtp ${Boost_LIBRARIES} ${DISRUPTOR_LIBRARY})
include_directories(ersap-vtp PUBLIC src/libsrc /usr/local/include
        ${Boost_INCLUDE_DIRS} ${DISRUPTOR_INCLUDE_DIR})


# Main Executables
add_executable(Aggregator src/libsrc/TwoStreamAggregator.cpp)
target_link_libraries(Aggregator ersap-vtp ${Boost_LIBRARIES} ${DISRUPTOR_LIBRARY} pthread dl z m )

add_executable(DisruptorTrial src/libsrc/disruptorTrial.cpp)
target_link_libraries(DisruptorTrial ersap-vtp ${Boost_LIBRARIES} ${DISRUPTOR_LIBRARY} pthread dl z m )


# Installation defaulting to ${CMAKE_INSTALL_PREFIX}/lib
install(TARGETS ersap-vtp LIBRARY)

# Installation defaulting to ${CMAKE_INSTALL_PREFIX}/include
install(FILES ${CPP_HEADER_FILES} DESTINATION include)

