package org.jlab.epsci.stream.util;

public class VRingBuffer<E> {

    private int capacity;
    private final int DEFAULT_CAPACITY = 128;
    private E[] data;
    private int writeSequence;
    private int readSequence;

    public VRingBuffer(int capacity) {
        this.capacity = (capacity < 1) ? DEFAULT_CAPACITY : capacity;
        this.data = (E[]) new Object[this.capacity];
        this.readSequence = 0;
        this.writeSequence = -1;
    }

    public void put(E element) {
            int nextWriteSeq = writeSequence + 1;
            data[nextWriteSeq % capacity] = element;
            writeSequence++;
    }
    public E get() {
        boolean isEmpty = writeSequence < readSequence;
        if (!isEmpty) {
            E nextValue = data[readSequence % capacity];
            readSequence++;
            return nextValue;
        } else {
            readSequence++;
        }
        return null;
    }
}
