# ersap-vtp
JLAB VTP stream handler package.

##########################################################
# If on the Jefferson Lab CUE system using Redhat 7:
##########################################################

###########################################
# For C++ code you'll need a compiler that is more advanced than the provided 4.8.5.
# This is necesary in order to compile and use the Disruptor library.
# Do this by calling "use".
# In this case switch to version 7.2.0. Other versions are available:
###########################################
use gcc/7.20

###########################################
# This C++ code requires Boost libraries.
# The cmake file will look for these libs.
###########################################

###########################################
# This C++ code requires the Disruptor library.
# So the first thing you need to do get and compile it:
###########################################
  git clone https://github.com/JeffersonLab/Disruptor-cpp.git
  cd Disruptor-cpp
  mkdir build
  cd build
  cmake .. -DCMAKE_BUILD_TYPE=Release  -DCMAKE_C_COMPILER=/apps/gcc/7.2.0/bin/gcc  -DCMAKE_CXX_COMPILER=/apps/gcc/7.2.0/bin/g++
  make
  setenv DISRUPTOR_CPP_HOME <../>


# If this is being compiled on a Mac the specification of the compiler can be removed:
  cmake .. -DCMAKE_BUILD_TYPE=Release


###########################################
# Now getting back to compiling ersap-vtp.
# For C++ compilation be sure the DISRUPTOR_CPP_HOME env var is set.
# The do the following on Redhat 7:
###########################################
  use gcc/7.2.0
  git clone https://github.com/JeffersonLab/ersap-vtp.git
  cd ersap-vtp
  mkdir -p build/release
  cd build/release
  cmake ../.. -DCMAKE_BUILD_TYPE=release -CCMAKE_CXX_COMPILER=/apps/gcc/7.2.0/bin/g++
  make


# If you want to install lib and executables into a specific directory,
# replace the last 2 calls with the following:
  cmake ../.. -DCMAKE_BUILD_TYPE=release -CCMAKE_CXX_COMPILER=/apps/gcc/7.2.0/bin/g++ -DINSTALL_DIR=<dir>
  make install


###########################################
# If on MacOS:
###########################################
  git clone https://github.com/JeffersonLab/ersap-vtp.git
  cd ersap-vtp
  mkdir -p build/release
  cd build/release
  cmake ../.. -DCMAKE_BUILD_TYPE=release -DINSTALL_DIR=<dir>
  make


#########################################################################
# For a debug version, replace the word "release" with the word "debug"
#########################################################################
  mkdir -p build/debug
  cd build/debug
  cmake ../.. -DCMAKE_BUILD_TYPE=debug -CCMAKE_CXX_COMPILER=/apps/gcc/7.2.0/bin/g++


###########################################
# On indra-s1
# This node needs extra instruction since
# it was setup in a weird way with 2 versions of boost
###########################################
# When building the disruptor
  bash
  source /op[t/rh/devtoolseb-7/enable
  git clone https://github.com/JeffersonLab/Disruptor-cpp.git
  cd Disruptor-cpp
  mkdir build
  cd build
  cmake .. -DCMAKE_BUILD_TYPE=release -DBOOST_LIBRARYDIR=/usr/lib64/boost169 -DBOOST_INCLUDEDIR=/usr/include/boost169
  make
  setenv DISRUPTOR_CPP_HOME <../>

# When building ersap-vtp
  git clone https://github.com/JeffersonLab/ersap-vtp.git
  cd ersap-vtp
  mkdir -p build/release
  cd build/release
  cmake ../.. -DCMAKE_BUILD_TYPE=release -DBOOST_LIBRARYDIR=/usr/lib64/boost169 -DBOOST_INCLUDEDIR=/usr/include/boost169
  make

# If the last (make) command fails, it's because calling cmake for ersap
# will relink the disruptor lib to the wrong boost libraries.
# To fix this, got back to the disruptor and remake it.
  cd <Disruptor_home>/build
  rm CMakeCache.txt
  cmake .. -DCMAKE_BUILD_TYPE=release -DBOOST_LIBRARYDIR=/usr/lib64/boost169 -DBOOST_INCLUDEDIR=/usr/include/boost169
  make

# Now go back to ersap and finish
  cd <ersap_home>/build/release
  make
  
