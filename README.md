# ersap-vtp
JLAB VTP stream handler package.

###########################################
# If on the Jefferson Lab CUE system using Redhat 7:
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
1)	git clone https://github.com/Abc-Arbitrage/Disruptor-cpp.git
2)	cd Disruptor-cpp
3)	git checkout d87e083
4)	patch –s –p0 < <ersap-vtp-top-dir>/disruptor-cpp.patch
5)	mkdir build
6)	cd build
8)	cmake .. -DCMAKE_BUILD_TYPE=Release  -DCMAKE_C_COMPILER=/apps/gcc/7.2.0/bin/gcc  -DCMAKE_CXX_COMPILER=/apps/gcc/7.2.0/bin/g++
9)	make
10)	setenv DISRUPTOR_CPP_HOME <../>


# If this is being compiled on a Mac the specification of the compiler can be removed:
8)	cmake .. -DCMAKE_BUILD_TYPE=Release


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


# if you want to install lib and executables into a specific directory,
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


