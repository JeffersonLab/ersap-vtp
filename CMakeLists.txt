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
find_package(Boost 1.69 REQUIRED system thread chrono)

if (Boost_FOUND)
    message(STATUS "Boost Found: libs = " ${Boost_LIBRARIES} ", include dirs = " ${Boost_INCLUDE_DIRS})
else()
    message(FATAL_ERROR "Boost cannot be found, cmake will exit." )
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
    message(STATUS "Disruptor inc dir = " ${DISRUPTOR_INCLUDE_DIR})
else()
    message(FATAL_ERROR "Disruptor.h NOT found, cmake will exit." )
endif()


if( DISRUPTOR_LIBRARY )
    message(STATUS "Disruptor lib = " ${DISRUPTOR_LIBRARY})
else()
    message(FATAL_ERROR "Disruptor library NOT found, cmake will exit." )
endif()

# Set the include directories
include_directories(${DISRUPTOR_INCLUDE_DIR} ${Boost_INCLUDE_DIRS} src/libsrc)

# Shared ersap C++ library
add_library(ersap-vtp SHARED ${CPP_LIB_FILES})
target_link_libraries(ersap-vtp ${DISRUPTOR_LIBRARY} ${Boost_LIBRARIES} pthread dl m)

# Set the libs to link against
link_libraries(ersap-vtp ${DISRUPTOR_LIBRARY} ${Boost_LIBRARIES} pthread dl m)

# Main Executables
add_executable(aggregator src/libsrc/TwoStreamAggregator.cpp)
add_executable(disruptorTrial src/libsrc/disruptorTrial.cpp)
add_executable(fakeVtp src/libsrc/FakeVtp.cpp)


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

