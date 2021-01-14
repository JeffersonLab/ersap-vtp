package org.jlab.epsci.ersap.vtp;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class RingEvent {
    private int streamId;
//    private BigInteger recordNumber;
    private Long recordNumber;
    private byte[] payload;
    private ByteBuffer payloadBuffer;
    private int payloadDataLength;
    private List<Integer> payloadData = new ArrayList<>();

    public RingEvent() {
        payload = new byte[100000];
        payloadBuffer = ByteBuffer.wrap(payload);
        payloadBuffer.order(ByteOrder.LITTLE_ENDIAN);
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

    public ByteBuffer getPayloadBuffer(){
        return payloadBuffer;
    }

    public List<Integer> getPayloadDataContainer(){
        payloadData.clear();
        return payloadData;
    }

    public List<Integer> getPayloadData(){
        return payloadData;
    }

}
