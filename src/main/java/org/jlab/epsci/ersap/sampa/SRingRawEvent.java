package org.jlab.epsci.ersap.sampa;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SRingRawEvent {

    private int chipAddress;
    private int channelAddress;

    private int windowTime;
    private byte[] payload;
    private ByteBuffer payloadBuffer;
    private int payloadDataLength;
    private int partLength1;
    private int partLength2;

    public SRingRawEvent() {
        payload = new byte[400000];
        payloadBuffer = ByteBuffer.wrap(payload);
        payloadBuffer.order(ByteOrder.LITTLE_ENDIAN);

    }

    public int getChipAddress() {
        return chipAddress;
    }

    public void setChipAddress(int chipAddress) {
        this.chipAddress = chipAddress;
    }

    public int getChannelAddress() {
        return channelAddress;
    }

    public void setChannelAddress(int channelAddress) {
        this.channelAddress = channelAddress;
    }

    public int getWindowTime() {
        return windowTime;
    }

    public void setWindowTime(int windowTime) {
        this.windowTime = windowTime;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public ByteBuffer getPayloadBuffer() {
        return payloadBuffer;
    }

    public void setPayloadBuffer(ByteBuffer payloadBuffer) {
        this.payloadBuffer = payloadBuffer;
    }

    public int getPayloadDataLength() {
        return payloadDataLength;
    }

    public void setPayloadDataLength(int payloadDataLength) {
        this.payloadDataLength = payloadDataLength;
    }

    public int getPartLength1() {
        return partLength1;
    }

    public void setPartLength1(int partLength1) {
        this.partLength1 = partLength1;
    }

    public int getPartLength2() {
        return partLength2;
    }

    public void setPartLength2(int partLength2) {
        this.partLength2 = partLength2;
    }
}
