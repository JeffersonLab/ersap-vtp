/*
 * Copyright (c) 2021, Jefferson Science Associates, all rights reserved.
 * See License.txt file.
 *
 * Thomas Jefferson National Accelerator Facility
 * Experimental Physics Software and Computing Infrastructure Group
 *
 * 12000, Jefferson Ave, Newport News, VA 23606
 * Phone : (757)-269-7100
 */

package org.jlab.epsci.stream.sampaBB;

import java.nio.ByteBuffer;

public class SRingRawEvent {

    private int blockNumber;

    /** Keep track of valid entries in localData. */
    private int localDataSize;

    /** One BB for each of 28 eLinks, 2 sets of eLinks - one for each fast stream. DSP mode. */
    private ByteBuffer[] localBBs = new ByteBuffer[28];


    /** One BB for each of 80 channels in DAS mode. */
    private ByteBuffer[] localBBs2 = new ByteBuffer[80];


    public SRingRawEvent() {
        // Allocate some memory to start

        // TODO: Can this size be reduced ??? Made the same as for DAS mode???
        for (int i=0; i < localBBs.length; i++) {
            localBBs[i] = ByteBuffer.allocate(131072);   // 32768 ints
        }

        // There should be only 16384 bytes / sampaData buffer because:
        // 16384 * 80 = 1,310,720 MB which is exactly what we have in
        // the 5 data ByteBuffers of 262144 bytes each.
        for (int i=0; i < localBBs2.length; i++) {
            localBBs2[i] = ByteBuffer.allocate(5*(4*65536)/80);   // 8192 shorts
        }
    }


    public ByteBuffer[] getDasBBs() {return localBBs2;}


    /**
     * Expand a particular ByteBuffer, preserving existing data.
     *
     * @param index index into localBBs array of ByteBuffers.
     * @param size number of bytes in new buffer.
     * @return buffer of increased size (same buffer if size is &lt;= current size).
     */
    public ByteBuffer expandBB(int index, int size) {
        ByteBuffer temp = localBBs[index];
        if (size <= temp.capacity()) {
            return temp;
        }
        // Expand BB by 25% over necessary amount to reduce future reallocations
        size = 5 * size / 4;
        localBBs[index] = ByteBuffer.allocate(size);
        // Copy over existing data, pos = 0 to limit
        System.arraycopy(temp, 0, localBBs[index], 0, temp.limit());
        return localBBs[index];
    }


    /**
     * Get the block number.
     * @return block number.
     */
    public int getBlockNumber() {return blockNumber;}

    /**
     * Set the block number.
     * @param blockNumber block number.
     */
    public void setBlockNumber(int blockNumber) {this.blockNumber = blockNumber;}


    /**
     * Get the internal array of ByteBuffers.
     * @return internal array of ByteBuffers.
     */
    public ByteBuffer[] getBBData() {return localBBs;}

    /**
     * Get the ByteBuffer associated with a given eLink.
     * @param eLink electronic link over which this data came (0-27).
     * @return ByteBuffer to store data in.
     */
    public ByteBuffer getBB(int eLink) {return localBBs[eLink];}

    /**
     * <p>Copy the data of an array of ByteBuffers into an internal array of the same.
     * Only up to the internal array capacity number of buffers are copied.</p>
     * This method assumes that the data arg has contiguous non-null data entries.
     *
     * @param data array of ByteBuffers containing data.
     *             All elements at and after a null buffer are ignored.
     */
    public void setBBData(ByteBuffer[] data) {
        localDataSize = 0;
        int dataEntries = Math.min(data.length, localBBs.length);
        for (int i = 0; i < dataEntries; i++) {
            // Ignore everything at and after a null buf
            if (data[i] == null) {
                System.out.println("SRingRawEvent: Error, trying to add null buffer");
                break;
            }

            // Need to actually copy the data here since the data in the arg
            // will change as soon as it reads more from the source.
            // This should be much faster than using Vectors or ArrayLists element by element
            // (object by object)
            System.arraycopy(data[i].array(), 0, localBBs[i].array(), 0, data[i].remaining());
            localDataSize++;
        }
    }

    /**
     * Copy data from ByteBuffer into the internal array of such buffers.
     * Excess elements (when internal array is full) of the input array are ignored.
     *
     * @param data array of ByteBuffers containing data.
     *             All elements at and after a null buffer are ignored.
     */
    public void addBBData(ByteBuffer[] data) {
        if (localDataSize >= localBBs.length) {
            System.out.println("SRingRawEvent: Error, RingBuffer data array limit reached.");
        } else {
            int dataEntries = Math.min(data.length, (localBBs.length - localDataSize));
            for (int i = 0; i < dataEntries; i++) {
                // Ignore everything at and after a null buf
                if (data[i] == null) {
                    System.out.println("SRingRawEvent: Error, trying to add null buffer");
                    break;
                }

                System.arraycopy(data[i].array(), 0,
                                 localBBs[localDataSize++].array(), 0,
                                 data[i].remaining());
            }
        }
    }

    /** Clear the buffer stored internally. */
    public void reset() {
        for (ByteBuffer buf : localBBs) {
            if (buf != null) {
                buf.clear();
            }
        }
    }

}
