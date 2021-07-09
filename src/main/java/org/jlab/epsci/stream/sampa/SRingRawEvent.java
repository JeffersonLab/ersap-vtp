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

package org.jlab.epsci.stream.sampa;

import java.util.ArrayList;

public class SRingRawEvent {

    private int blockNumber;
    @SuppressWarnings("unchecked")
    private final ArrayList<Integer>[] localData = new ArrayList[56];
    /** Keep track of valid entries in localData. */
    private int localDataSize;




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
     * Get the internal array of Vectors of Integers.
     * @return internal array of Vectors of Integers.
     */
    public ArrayList<Integer>[] getData() {
        return localData;
    }

    /**
     * <p>Copy the references of a an array of Vectors contain Integers
     * into an internal array of the same. Only up to the internal array
     * capacity number of arg vectors are copied.</p>
     * This method assumes that data has contiguous non-null data entries.
     *
     * @param data array of vectors containing Integer objects.
     *             All elements at and after a null vector are ignored.
     */
    public void setData(ArrayList<Integer>[] data) {
        localDataSize = 0;
        int dataEntries = Math.min(data.length, localData.length);
        for (int i = 0; i < dataEntries; i++) {
            // Ignore everything at and after a null vector
            if (data[i] == null) {
                System.out.println("SRingRawEvent: Error, trying to add null vector");
                break;
            }

            localData[i] = data[i];
            localDataSize++;
        }
    }

    /**
     * Copy vectors of Integers into the internal array of such vectors.
     * Excess elements (when internal array is full) of the input array are ignored.
     *
     * @param data array of vectors containing Integer objects.
     *             All elements at and after a null vector are ignored.
     */
    public void addData(ArrayList<Integer>[] data) {
        if (localDataSize >= localData.length) {
            System.out.println("SRingRawEvent: Error, RingBuffer data array limit reached.");
        } else {
            int dataEntries = Math.min(data.length, (localData.length - localDataSize));
            for (int i = 0; i < dataEntries; i++) {
                // Ignore everything at and after a null vector
                if (data[i] == null) {
                    System.out.println("SRingRawEvent: Error, trying to add null vector");
                    break;
                }

                localData[localDataSize++] = data[i];
            }
        }
    }

    /** Clear the Vectors (of Integers) stored internally. */
    public void reset() {
        for (ArrayList<Integer> list : localData) {
            if (list != null) {
                list.clear();
            }
        }
    }
}
