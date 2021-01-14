package org.jlab.epsci.ersap.vtp;

import java.math.BigInteger;

public class RingEvent {
    private int streamId;
//    private BigInteger recordNumber;
    private Long recordNumber;
    private byte[] payload;
    private int payloadDataLength;

    public RingEvent() {
        payload = new byte[100000];
    }

    public int getStreamId() {
        return streamId;
    }

    public void setStreamId(int streamId) {
        this.streamId = streamId;
    }

//    public BigInteger getRecordNumber() {
//        return recordNumber;
//    }
//
//    public void setRecordNumber(BigInteger recordNumber) {
//        this.recordNumber = recordNumber;
//    }

    public Long getRecordNumber() {
        return recordNumber;
    }

    public void setRecordNumber(Long recordNumber) {
        this.recordNumber = recordNumber;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public int getPayloadDataLength() {
        return payloadDataLength;
    }

    public void setPayloadDataLength(int payloadDataLength) {
        this.payloadDataLength = payloadDataLength;
    }
}
