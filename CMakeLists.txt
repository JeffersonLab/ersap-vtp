# To use this, do the following the first time:
#   % mkdir -p build/release
#   % cd build/release
#   % cmake ../..
#   % make (install)
#
#
# After doing this once can do the following to recompile
#  % cd <top level evio dir>
#  % cmake --build build/release (--target install)
#
#
# Set the path in which to install.
# This can be overridden on command line (in build/cmake dir) with:
#   % cmake -DCMAKE_INSTALL_PREFIX=<my_dir> ../..
#   % make install


# (This call must be placed BEFORE "project" command).
cmake_minimum_required(VERSION 3.14)

project(ersap_vtp VERSION 1.0)

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED True)
set(CMAKE_DEBUG_POSTFIX -dbg)
set(CMAKE_INSTALL_PREFIX ./)


# Place libs & binaries in build/release/lib and bin
set(LIBRARY_OUTPUT_PATH    ${CMAKE_BINARY_DIR}/lib)
set(EXECUTABLE_OUTPUT_PATH ${CMAKE_BINARY_DIR}/bin)


# Set architecture-related string for installation
set(ARCH ${CMAKE_HOST_SYSTEM_NAME}-${CMAKE_HOST_SYSTEM_PROCESSOR})
message(STATUS "ARCH = " ${ARCH})


# Install into $INSTALL_DIR directory
if (DEFINED INSTALL_DIR)
    message(STATUS "INSTALL DIR = " ${INSTALL_DIR})
    set(INSTALL_DIR_DEFINED 1)
    set(CMAKE_INSTALL_PREFIX ${INSTALL_DIR}/${ARCH})
    set(CMAKE_INSTALL_INCLUDEDIR ${INSTALL_DIR}/common/include)
    message(STATUS "CMAKE_INSTALL_PREFIX set to " ${CMAKE_INSTALL_PREFIX})
else()
    message(STATUS "Specify -DINSTALL_DIR=... on the command line or no installation possible!")
endif()


set(CPP_HEADER_FILES
        src/libsrc/ByteOrder.h
        src/libsrc/ByteBuffer.h
        src/libsrc/EvioException.h
        src/libsrc/disruptorTrial.h
        src/libsrc/RingEvent.h
        src/libsrc/Receiver.h
        src/libsrc/Consumer.h
        src/libsrc/EUtil.h
        src/libsrc/FakeVtp.h
        src/libsrc/ChargeTime.h
        src/libsrc/Aggregator.h
        src/libsrc/TwoStreamAggregator.h
        )

set(CPP_LIB_FILES
        src/libsrc/ByteOrder.cpp
        src/libsrc/ByteBuffer.cpp
        src/libsrc/RingEvent.cpp
        )


include(FindPackageHandleStandardArgs)


# search for boost libs
find_package(Boost REQUIRED system thread chrono)

if (Boost_FOUND)
    message(STATUS "Boost Found: libs = " ${Boost_LIBRARIES} ", include dirs = " ${Boost_INCLUDE_DIRS})
    link_directories(${Boost_LIBRARY_DIRS})
else()
    # on indra-s1
    set(Boost_INCLUDE_DIRS /usr/include/boost)

    find_library(BOOST_THD_LIB NAMES libboost_thread.so.1.69.0 PATHS /usr/lib64)
    find_library(BOOST_CHR_LIB NAMES libboost_chrono.so.1.69.0 PATHS /usr/lib64)
    find_library(BOOST_SYS_LIB NAMES libboost_filesystem.so.1.69.0 PATHS /usr/lib64)

    set(Boost_LIBRARIES ${BOOST_THD_LIB} " " ${BOOST_CHR_LIB} " " ${BOOST_SYS_LIB})
    if (BOOST_THD_LIB)
        message(STATUS "Boost version 1.69 Found: libs = " ${Boost_LIBRARIES} ", include dirs = " ${BOOST_INCLUDE_DIR})
        link_directories(/lusr/lib64)
    else()
        message(FATAL_ERROR "Boost cannot be found, cmake will exit." )
    endif()
endif()

find_library(PTHREAD_LIBRARY
        NAMES libpthread-2.17.so
        PATHS /usr/lib64
        NO_DEFAULT_PATH
        )

if (PTHREAD_LIBRARY)
    message(STATUS "Pthread lib found: libs = " ${PTHREAD_LIBRARY})
else()
    set(PTHREAD_LIBRARY pthread)
    message(STATUS "pthread cannot be found, try to continue anyway." )
endif()


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
add_executable(aggregator src/libsrc/TwoStreamAggregator.cpp)
target_link_libraries(aggregator ersap-vtp ${Boost_LIBRARIES} ${DISRUPTOR_LIBRARY} ${PTHREAD_LIBRARY} dl z m )

add_executable(disruptorTrial src/libsrc/disruptorTrial.cpp)
target_link_libraries(disruptorTrial ersap-vtp ${Boost_LIBRARIES} ${DISRUPTOR_LIBRARY} ${PTHREAD_LIBRARY} dl z m )

add_executable(fakeVtp src/libsrc/FakeVtp.cpp)
target_link_libraries(fakeVtp ersap-vtp ${Boost_LIBRARIES} dl z m )


# Installation defaulting to ${CMAKE_INSTALL_PREFIX}/lib
install(TARGETS ersap-vtp LIBRARY)

# Installation defaulting to ${CMAKE_INSTALL_PREFIX}/include
install(FILES ${CPP_HEADER_FILES} DESTINATION include)

# Uninstall target
if(NOT TARGET uninstall)
    configure_file(
            "${CMAKE_CURRENT_SOURCE_DIR}/cmake_uninstall.cmake.in"
            "${CMAKE_CURRENT_BINARY_DIR}/cmake_uninstall.cmake" IMMEDIATE @ONLY)

    add_custom_target(uninstall
            COMMAND ${CMAKE_COMMAND} -P ${CMAKE_CURRENT_BINARY_DIR}/cmake_uninstall.cmake)
endif()

