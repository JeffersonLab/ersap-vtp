package org.jlab.epsci.stream.vtp.evio;

/**
 * Copyright (c) 2021, Jefferson Science Associates, all rights reserved.
 * See LICENSE.txt file.
 * Thomas Jefferson National Accelerator Facility
 * Experimental Physics Software and Computing Infrastructure Group
 * 12000, Jefferson Ave, Newport News, VA 23606
 * Phone : (757)-269-7100
 *
 * @author timmer on 1/18/22
 * @project ersap-vtp
 */

import org.jlab.coda.jevio.ByteDataTransformer;
import org.jlab.coda.jevio.EvioException;

import java.nio.BufferUnderflowException;
import java.nio.ByteOrder;
import java.util.Objects;

/** Class to parse and hold SRO header data. */
class CODA_SRO_Header {

    // Allocate these helper arrays only once
    static  int[]  intInput = new  int[7];
    static long[] longInput = new long[3];

    int source_id;
    int total_length;
    int payload_length;
    int compressed_length;
    int magic;
    int format_version;
    int flags;

    long record_counter;
    long ts_sec;
    long ts_nsec;


    /**
     * Get the byte offset to the magic number when reading header.
     * @return byte offset to the magic number when reading header.
     */
    static public int getMagicOffset() {return 16;}


    /**
     * Read a buffer containing raw fadc and tdc data in SRO format
     * and extract an SRO header.
     *
     * @param data  data to read.
     * @param off   byte offset into the data byte array.
     * @param order byte order in which to interpret the data.
     * @throws EvioException if issues reading the data.
     * @throws BufferUnderflowException if too little data.
     */
    public void read(byte[] data, int off, ByteOrder order) throws EvioException {
        Objects.requireNonNull(data);
        if (data.length < 52 + off) {
            System.out.println("CODA_SRO_Header: only have " + data.length + " bytes, need " + (52+off));
            throw new BufferUnderflowException();
        }

        // ints
        ByteDataTransformer.toIntArray(data, off, 28, order, intInput, 0);
        source_id = intInput[0];
        total_length = intInput[1];
        payload_length = intInput[2];
        compressed_length = intInput[3];
        magic = intInput[4];
        format_version = intInput[5];
        flags = intInput[6];

        // longs
        ByteDataTransformer.toLongArray(data, off+28, 24, order, longInput, 0);
        record_counter = longInput[0];
        ts_sec = longInput[1];
        ts_nsec = longInput[2];
    }
}
