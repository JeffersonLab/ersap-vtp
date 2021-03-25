package org.jlab.epsci.ersap.vtp.engines.format;

public final class AdcDataFormat {
  private AdcDataFormat() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface AdcHitMapOrBuilder extends
      // @@protoc_insertion_point(interface_extends:AdcHitMap)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 crate = 1;</code>
     * @return A list containing the crate.
     */
    java.util.List<java.lang.Integer> getCrateList();
    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 crate = 1;</code>
     * @return The count of crate.
     */
    int getCrateCount();
    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 crate = 1;</code>
     * @param index The index of the element to return.
     * @return The crate at the given index.
     */
    int getCrate(int index);

    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 slot = 2;</code>
     * @return A list containing the slot.
     */
    java.util.List<java.lang.Integer> getSlotList();
    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 slot = 2;</code>
     * @return The count of slot.
     */
    int getSlotCount();
    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 slot = 2;</code>
     * @param index The index of the element to return.
     * @return The slot at the given index.
     */
    int getSlot(int index);

    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 channel = 3;</code>
     * @return A list containing the channel.
     */
    java.util.List<java.lang.Integer> getChannelList();
    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 channel = 3;</code>
     * @return The count of channel.
     */
    int getChannelCount();
    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 channel = 3;</code>
     * @param index The index of the element to return.
     * @return The channel at the given index.
     */
    int getChannel(int index);

    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 charge = 4;</code>
     * @return A list containing the charge.
     */
    java.util.List<java.lang.Integer> getChargeList();
    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 charge = 4;</code>
     * @return The count of charge.
     */
    int getChargeCount();
    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 charge = 4;</code>
     * @param index The index of the element to return.
     * @return The charge at the given index.
     */
    int getCharge(int index);

    /**
     * <pre>
     * variable length signed int64
     * </pre>
     *
     * <code>repeated sint64 time = 5;</code>
     * @return A list containing the time.
     */
    java.util.List<java.lang.Long> getTimeList();
    /**
     * <pre>
     * variable length signed int64
     * </pre>
     *
     * <code>repeated sint64 time = 5;</code>
     * @return The count of time.
     */
    int getTimeCount();
    /**
     * <pre>
     * variable length signed int64
     * </pre>
     *
     * <code>repeated sint64 time = 5;</code>
     * @param index The index of the element to return.
     * @return The time at the given index.
     */
    long getTime(int index);
  }
  /**
   * <pre>
   * ... Data class ...
   * </pre>
   *
   * Protobuf type {@code AdcHitMap}
   */
  public static final class AdcHitMap extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:AdcHitMap)
      AdcHitMapOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use AdcHitMap.newBuilder() to construct.
    private AdcHitMap(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private AdcHitMap() {
      crate_ = emptyIntList();
      slot_ = emptyIntList();
      channel_ = emptyIntList();
      charge_ = emptyIntList();
      time_ = emptyLongList();
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new AdcHitMap();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private AdcHitMap(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      int mutable_bitField0_ = 0;
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 8: {
              if (!((mutable_bitField0_ & 0x00000001) != 0)) {
                crate_ = newIntList();
                mutable_bitField0_ |= 0x00000001;
              }
              crate_.addInt(input.readSInt32());
              break;
            }
            case 10: {
              int length = input.readRawVarint32();
              int limit = input.pushLimit(length);
              if (!((mutable_bitField0_ & 0x00000001) != 0) && input.getBytesUntilLimit() > 0) {
                crate_ = newIntList();
                mutable_bitField0_ |= 0x00000001;
              }
              while (input.getBytesUntilLimit() > 0) {
                crate_.addInt(input.readSInt32());
              }
              input.popLimit(limit);
              break;
            }
            case 16: {
              if (!((mutable_bitField0_ & 0x00000002) != 0)) {
                slot_ = newIntList();
                mutable_bitField0_ |= 0x00000002;
              }
              slot_.addInt(input.readSInt32());
              break;
            }
            case 18: {
              int length = input.readRawVarint32();
              int limit = input.pushLimit(length);
              if (!((mutable_bitField0_ & 0x00000002) != 0) && input.getBytesUntilLimit() > 0) {
                slot_ = newIntList();
                mutable_bitField0_ |= 0x00000002;
              }
              while (input.getBytesUntilLimit() > 0) {
                slot_.addInt(input.readSInt32());
              }
              input.popLimit(limit);
              break;
            }
            case 24: {
              if (!((mutable_bitField0_ & 0x00000004) != 0)) {
                channel_ = newIntList();
                mutable_bitField0_ |= 0x00000004;
              }
              channel_.addInt(input.readSInt32());
              break;
            }
            case 26: {
              int length = input.readRawVarint32();
              int limit = input.pushLimit(length);
              if (!((mutable_bitField0_ & 0x00000004) != 0) && input.getBytesUntilLimit() > 0) {
                channel_ = newIntList();
                mutable_bitField0_ |= 0x00000004;
              }
              while (input.getBytesUntilLimit() > 0) {
                channel_.addInt(input.readSInt32());
              }
              input.popLimit(limit);
              break;
            }
            case 32: {
              if (!((mutable_bitField0_ & 0x00000008) != 0)) {
                charge_ = newIntList();
                mutable_bitField0_ |= 0x00000008;
              }
              charge_.addInt(input.readSInt32());
              break;
            }
            case 34: {
              int length = input.readRawVarint32();
              int limit = input.pushLimit(length);
              if (!((mutable_bitField0_ & 0x00000008) != 0) && input.getBytesUntilLimit() > 0) {
                charge_ = newIntList();
                mutable_bitField0_ |= 0x00000008;
              }
              while (input.getBytesUntilLimit() > 0) {
                charge_.addInt(input.readSInt32());
              }
              input.popLimit(limit);
              break;
            }
            case 40: {
              if (!((mutable_bitField0_ & 0x00000010) != 0)) {
                time_ = newLongList();
                mutable_bitField0_ |= 0x00000010;
              }
              time_.addLong(input.readSInt64());
              break;
            }
            case 42: {
              int length = input.readRawVarint32();
              int limit = input.pushLimit(length);
              if (!((mutable_bitField0_ & 0x00000010) != 0) && input.getBytesUntilLimit() > 0) {
                time_ = newLongList();
                mutable_bitField0_ |= 0x00000010;
              }
              while (input.getBytesUntilLimit() > 0) {
                time_.addLong(input.readSInt64());
              }
              input.popLimit(limit);
              break;
            }
            default: {
              if (!parseUnknownField(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        if (((mutable_bitField0_ & 0x00000001) != 0)) {
          crate_.makeImmutable(); // C
        }
        if (((mutable_bitField0_ & 0x00000002) != 0)) {
          slot_.makeImmutable(); // C
        }
        if (((mutable_bitField0_ & 0x00000004) != 0)) {
          channel_.makeImmutable(); // C
        }
        if (((mutable_bitField0_ & 0x00000008) != 0)) {
          charge_.makeImmutable(); // C
        }
        if (((mutable_bitField0_ & 0x00000010) != 0)) {
          time_.makeImmutable(); // C
        }
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return AdcDataFormat.internal_static_AdcHitMap_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return AdcDataFormat.internal_static_AdcHitMap_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              AdcDataFormat.AdcHitMap.class, AdcDataFormat.AdcHitMap.Builder.class);
    }

    public static final int CRATE_FIELD_NUMBER = 1;
    private com.google.protobuf.Internal.IntList crate_;
    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 crate = 1;</code>
     * @return A list containing the crate.
     */
    @java.lang.Override
    public java.util.List<java.lang.Integer>
        getCrateList() {
      return crate_;
    }
    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 crate = 1;</code>
     * @return The count of crate.
     */
    public int getCrateCount() {
      return crate_.size();
    }
    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 crate = 1;</code>
     * @param index The index of the element to return.
     * @return The crate at the given index.
     */
    public int getCrate(int index) {
      return crate_.getInt(index);
    }
    private int crateMemoizedSerializedSize = -1;

    public static final int SLOT_FIELD_NUMBER = 2;
    private com.google.protobuf.Internal.IntList slot_;
    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 slot = 2;</code>
     * @return A list containing the slot.
     */
    @java.lang.Override
    public java.util.List<java.lang.Integer>
        getSlotList() {
      return slot_;
    }
    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 slot = 2;</code>
     * @return The count of slot.
     */
    public int getSlotCount() {
      return slot_.size();
    }
    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 slot = 2;</code>
     * @param index The index of the element to return.
     * @return The slot at the given index.
     */
    public int getSlot(int index) {
      return slot_.getInt(index);
    }
    private int slotMemoizedSerializedSize = -1;

    public static final int CHANNEL_FIELD_NUMBER = 3;
    private com.google.protobuf.Internal.IntList channel_;
    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 channel = 3;</code>
     * @return A list containing the channel.
     */
    @java.lang.Override
    public java.util.List<java.lang.Integer>
        getChannelList() {
      return channel_;
    }
    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 channel = 3;</code>
     * @return The count of channel.
     */
    public int getChannelCount() {
      return channel_.size();
    }
    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 channel = 3;</code>
     * @param index The index of the element to return.
     * @return The channel at the given index.
     */
    public int getChannel(int index) {
      return channel_.getInt(index);
    }
    private int channelMemoizedSerializedSize = -1;

    public static final int CHARGE_FIELD_NUMBER = 4;
    private com.google.protobuf.Internal.IntList charge_;
    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 charge = 4;</code>
     * @return A list containing the charge.
     */
    @java.lang.Override
    public java.util.List<java.lang.Integer>
        getChargeList() {
      return charge_;
    }
    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 charge = 4;</code>
     * @return The count of charge.
     */
    public int getChargeCount() {
      return charge_.size();
    }
    /**
     * <pre>
     * variable length signed int32
     * </pre>
     *
     * <code>repeated sint32 charge = 4;</code>
     * @param index The index of the element to return.
     * @return The charge at the given index.
     */
    public int getCharge(int index) {
      return charge_.getInt(index);
    }
    private int chargeMemoizedSerializedSize = -1;

    public static final int TIME_FIELD_NUMBER = 5;
    private com.google.protobuf.Internal.LongList time_;
    /**
     * <pre>
     * variable length signed int64
     * </pre>
     *
     * <code>repeated sint64 time = 5;</code>
     * @return A list containing the time.
     */
    @java.lang.Override
    public java.util.List<java.lang.Long>
        getTimeList() {
      return time_;
    }
    /**
     * <pre>
     * variable length signed int64
     * </pre>
     *
     * <code>repeated sint64 time = 5;</code>
     * @return The count of time.
     */
    public int getTimeCount() {
      return time_.size();
    }
    /**
     * <pre>
     * variable length signed int64
     * </pre>
     *
     * <code>repeated sint64 time = 5;</code>
     * @param index The index of the element to return.
     * @return The time at the given index.
     */
    public long getTime(int index) {
      return time_.getLong(index);
    }
    private int timeMemoizedSerializedSize = -1;

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (getCrateList().size() > 0) {
        output.writeUInt32NoTag(10);
        output.writeUInt32NoTag(crateMemoizedSerializedSize);
      }
      for (int i = 0; i < crate_.size(); i++) {
        output.writeSInt32NoTag(crate_.getInt(i));
      }
      if (getSlotList().size() > 0) {
        output.writeUInt32NoTag(18);
        output.writeUInt32NoTag(slotMemoizedSerializedSize);
      }
      for (int i = 0; i < slot_.size(); i++) {
        output.writeSInt32NoTag(slot_.getInt(i));
      }
      if (getChannelList().size() > 0) {
        output.writeUInt32NoTag(26);
        output.writeUInt32NoTag(channelMemoizedSerializedSize);
      }
      for (int i = 0; i < channel_.size(); i++) {
        output.writeSInt32NoTag(channel_.getInt(i));
      }
      if (getChargeList().size() > 0) {
        output.writeUInt32NoTag(34);
        output.writeUInt32NoTag(chargeMemoizedSerializedSize);
      }
      for (int i = 0; i < charge_.size(); i++) {
        output.writeSInt32NoTag(charge_.getInt(i));
      }
      if (getTimeList().size() > 0) {
        output.writeUInt32NoTag(42);
        output.writeUInt32NoTag(timeMemoizedSerializedSize);
      }
      for (int i = 0; i < time_.size(); i++) {
        output.writeSInt64NoTag(time_.getLong(i));
      }
      unknownFields.writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      {
        int dataSize = 0;
        for (int i = 0; i < crate_.size(); i++) {
          dataSize += com.google.protobuf.CodedOutputStream
            .computeSInt32SizeNoTag(crate_.getInt(i));
        }
        size += dataSize;
        if (!getCrateList().isEmpty()) {
          size += 1;
          size += com.google.protobuf.CodedOutputStream
              .computeInt32SizeNoTag(dataSize);
        }
        crateMemoizedSerializedSize = dataSize;
      }
      {
        int dataSize = 0;
        for (int i = 0; i < slot_.size(); i++) {
          dataSize += com.google.protobuf.CodedOutputStream
            .computeSInt32SizeNoTag(slot_.getInt(i));
        }
        size += dataSize;
        if (!getSlotList().isEmpty()) {
          size += 1;
          size += com.google.protobuf.CodedOutputStream
              .computeInt32SizeNoTag(dataSize);
        }
        slotMemoizedSerializedSize = dataSize;
      }
      {
        int dataSize = 0;
        for (int i = 0; i < channel_.size(); i++) {
          dataSize += com.google.protobuf.CodedOutputStream
            .computeSInt32SizeNoTag(channel_.getInt(i));
        }
        size += dataSize;
        if (!getChannelList().isEmpty()) {
          size += 1;
          size += com.google.protobuf.CodedOutputStream
              .computeInt32SizeNoTag(dataSize);
        }
        channelMemoizedSerializedSize = dataSize;
      }
      {
        int dataSize = 0;
        for (int i = 0; i < charge_.size(); i++) {
          dataSize += com.google.protobuf.CodedOutputStream
            .computeSInt32SizeNoTag(charge_.getInt(i));
        }
        size += dataSize;
        if (!getChargeList().isEmpty()) {
          size += 1;
          size += com.google.protobuf.CodedOutputStream
              .computeInt32SizeNoTag(dataSize);
        }
        chargeMemoizedSerializedSize = dataSize;
      }
      {
        int dataSize = 0;
        for (int i = 0; i < time_.size(); i++) {
          dataSize += com.google.protobuf.CodedOutputStream
            .computeSInt64SizeNoTag(time_.getLong(i));
        }
        size += dataSize;
        if (!getTimeList().isEmpty()) {
          size += 1;
          size += com.google.protobuf.CodedOutputStream
              .computeInt32SizeNoTag(dataSize);
        }
        timeMemoizedSerializedSize = dataSize;
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof AdcDataFormat.AdcHitMap)) {
        return super.equals(obj);
      }
      AdcDataFormat.AdcHitMap other = (AdcDataFormat.AdcHitMap) obj;

      if (!getCrateList()
          .equals(other.getCrateList())) return false;
      if (!getSlotList()
          .equals(other.getSlotList())) return false;
      if (!getChannelList()
          .equals(other.getChannelList())) return false;
      if (!getChargeList()
          .equals(other.getChargeList())) return false;
      if (!getTimeList()
          .equals(other.getTimeList())) return false;
      if (!unknownFields.equals(other.unknownFields)) return false;
      return true;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      if (getCrateCount() > 0) {
        hash = (37 * hash) + CRATE_FIELD_NUMBER;
        hash = (53 * hash) + getCrateList().hashCode();
      }
      if (getSlotCount() > 0) {
        hash = (37 * hash) + SLOT_FIELD_NUMBER;
        hash = (53 * hash) + getSlotList().hashCode();
      }
      if (getChannelCount() > 0) {
        hash = (37 * hash) + CHANNEL_FIELD_NUMBER;
        hash = (53 * hash) + getChannelList().hashCode();
      }
      if (getChargeCount() > 0) {
        hash = (37 * hash) + CHARGE_FIELD_NUMBER;
        hash = (53 * hash) + getChargeList().hashCode();
      }
      if (getTimeCount() > 0) {
        hash = (37 * hash) + TIME_FIELD_NUMBER;
        hash = (53 * hash) + getTimeList().hashCode();
      }
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static AdcDataFormat.AdcHitMap parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static AdcDataFormat.AdcHitMap parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static AdcDataFormat.AdcHitMap parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static AdcDataFormat.AdcHitMap parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static AdcDataFormat.AdcHitMap parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static AdcDataFormat.AdcHitMap parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static AdcDataFormat.AdcHitMap parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static AdcDataFormat.AdcHitMap parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static AdcDataFormat.AdcHitMap parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static AdcDataFormat.AdcHitMap parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static AdcDataFormat.AdcHitMap parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static AdcDataFormat.AdcHitMap parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(AdcDataFormat.AdcHitMap prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * <pre>
     * ... Data class ...
     * </pre>
     *
     * Protobuf type {@code AdcHitMap}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:AdcHitMap)
        AdcDataFormat.AdcHitMapOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return AdcDataFormat.internal_static_AdcHitMap_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return AdcDataFormat.internal_static_AdcHitMap_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                AdcDataFormat.AdcHitMap.class, AdcDataFormat.AdcHitMap.Builder.class);
      }

      // Construct using AdcDataFormat.AdcHitMap.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        crate_ = emptyIntList();
        bitField0_ = (bitField0_ & ~0x00000001);
        slot_ = emptyIntList();
        bitField0_ = (bitField0_ & ~0x00000002);
        channel_ = emptyIntList();
        bitField0_ = (bitField0_ & ~0x00000004);
        charge_ = emptyIntList();
        bitField0_ = (bitField0_ & ~0x00000008);
        time_ = emptyLongList();
        bitField0_ = (bitField0_ & ~0x00000010);
        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return AdcDataFormat.internal_static_AdcHitMap_descriptor;
      }

      @java.lang.Override
      public AdcDataFormat.AdcHitMap getDefaultInstanceForType() {
        return AdcDataFormat.AdcHitMap.getDefaultInstance();
      }

      @java.lang.Override
      public AdcDataFormat.AdcHitMap build() {
        AdcDataFormat.AdcHitMap result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public AdcDataFormat.AdcHitMap buildPartial() {
        AdcDataFormat.AdcHitMap result = new AdcDataFormat.AdcHitMap(this);
        int from_bitField0_ = bitField0_;
        if (((bitField0_ & 0x00000001) != 0)) {
          crate_.makeImmutable();
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.crate_ = crate_;
        if (((bitField0_ & 0x00000002) != 0)) {
          slot_.makeImmutable();
          bitField0_ = (bitField0_ & ~0x00000002);
        }
        result.slot_ = slot_;
        if (((bitField0_ & 0x00000004) != 0)) {
          channel_.makeImmutable();
          bitField0_ = (bitField0_ & ~0x00000004);
        }
        result.channel_ = channel_;
        if (((bitField0_ & 0x00000008) != 0)) {
          charge_.makeImmutable();
          bitField0_ = (bitField0_ & ~0x00000008);
        }
        result.charge_ = charge_;
        if (((bitField0_ & 0x00000010) != 0)) {
          time_.makeImmutable();
          bitField0_ = (bitField0_ & ~0x00000010);
        }
        result.time_ = time_;
        onBuilt();
        return result;
      }

      @java.lang.Override
      public Builder clone() {
        return super.clone();
      }
      @java.lang.Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.setField(field, value);
      }
      @java.lang.Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return super.clearField(field);
      }
      @java.lang.Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return super.clearOneof(oneof);
      }
      @java.lang.Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return super.setRepeatedField(field, index, value);
      }
      @java.lang.Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.addRepeatedField(field, value);
      }
      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof AdcDataFormat.AdcHitMap) {
          return mergeFrom((AdcDataFormat.AdcHitMap)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(AdcDataFormat.AdcHitMap other) {
        if (other == AdcDataFormat.AdcHitMap.getDefaultInstance()) return this;
        if (!other.crate_.isEmpty()) {
          if (crate_.isEmpty()) {
            crate_ = other.crate_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureCrateIsMutable();
            crate_.addAll(other.crate_);
          }
          onChanged();
        }
        if (!other.slot_.isEmpty()) {
          if (slot_.isEmpty()) {
            slot_ = other.slot_;
            bitField0_ = (bitField0_ & ~0x00000002);
          } else {
            ensureSlotIsMutable();
            slot_.addAll(other.slot_);
          }
          onChanged();
        }
        if (!other.channel_.isEmpty()) {
          if (channel_.isEmpty()) {
            channel_ = other.channel_;
            bitField0_ = (bitField0_ & ~0x00000004);
          } else {
            ensureChannelIsMutable();
            channel_.addAll(other.channel_);
          }
          onChanged();
        }
        if (!other.charge_.isEmpty()) {
          if (charge_.isEmpty()) {
            charge_ = other.charge_;
            bitField0_ = (bitField0_ & ~0x00000008);
          } else {
            ensureChargeIsMutable();
            charge_.addAll(other.charge_);
          }
          onChanged();
        }
        if (!other.time_.isEmpty()) {
          if (time_.isEmpty()) {
            time_ = other.time_;
            bitField0_ = (bitField0_ & ~0x00000010);
          } else {
            ensureTimeIsMutable();
            time_.addAll(other.time_);
          }
          onChanged();
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        AdcDataFormat.AdcHitMap parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (AdcDataFormat.AdcHitMap) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }
      private int bitField0_;

      private com.google.protobuf.Internal.IntList crate_ = emptyIntList();
      private void ensureCrateIsMutable() {
        if (!((bitField0_ & 0x00000001) != 0)) {
          crate_ = mutableCopy(crate_);
          bitField0_ |= 0x00000001;
         }
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 crate = 1;</code>
       * @return A list containing the crate.
       */
      public java.util.List<java.lang.Integer>
          getCrateList() {
        return ((bitField0_ & 0x00000001) != 0) ?
                 java.util.Collections.unmodifiableList(crate_) : crate_;
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 crate = 1;</code>
       * @return The count of crate.
       */
      public int getCrateCount() {
        return crate_.size();
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 crate = 1;</code>
       * @param index The index of the element to return.
       * @return The crate at the given index.
       */
      public int getCrate(int index) {
        return crate_.getInt(index);
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 crate = 1;</code>
       * @param index The index to set the value at.
       * @param value The crate to set.
       * @return This builder for chaining.
       */
      public Builder setCrate(
          int index, int value) {
        ensureCrateIsMutable();
        crate_.setInt(index, value);
        onChanged();
        return this;
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 crate = 1;</code>
       * @param value The crate to add.
       * @return This builder for chaining.
       */
      public Builder addCrate(int value) {
        ensureCrateIsMutable();
        crate_.addInt(value);
        onChanged();
        return this;
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 crate = 1;</code>
       * @param values The crate to add.
       * @return This builder for chaining.
       */
      public Builder addAllCrate(
          java.lang.Iterable<? extends java.lang.Integer> values) {
        ensureCrateIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, crate_);
        onChanged();
        return this;
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 crate = 1;</code>
       * @return This builder for chaining.
       */
      public Builder clearCrate() {
        crate_ = emptyIntList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
        return this;
      }

      private com.google.protobuf.Internal.IntList slot_ = emptyIntList();
      private void ensureSlotIsMutable() {
        if (!((bitField0_ & 0x00000002) != 0)) {
          slot_ = mutableCopy(slot_);
          bitField0_ |= 0x00000002;
         }
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 slot = 2;</code>
       * @return A list containing the slot.
       */
      public java.util.List<java.lang.Integer>
          getSlotList() {
        return ((bitField0_ & 0x00000002) != 0) ?
                 java.util.Collections.unmodifiableList(slot_) : slot_;
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 slot = 2;</code>
       * @return The count of slot.
       */
      public int getSlotCount() {
        return slot_.size();
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 slot = 2;</code>
       * @param index The index of the element to return.
       * @return The slot at the given index.
       */
      public int getSlot(int index) {
        return slot_.getInt(index);
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 slot = 2;</code>
       * @param index The index to set the value at.
       * @param value The slot to set.
       * @return This builder for chaining.
       */
      public Builder setSlot(
          int index, int value) {
        ensureSlotIsMutable();
        slot_.setInt(index, value);
        onChanged();
        return this;
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 slot = 2;</code>
       * @param value The slot to add.
       * @return This builder for chaining.
       */
      public Builder addSlot(int value) {
        ensureSlotIsMutable();
        slot_.addInt(value);
        onChanged();
        return this;
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 slot = 2;</code>
       * @param values The slot to add.
       * @return This builder for chaining.
       */
      public Builder addAllSlot(
          java.lang.Iterable<? extends java.lang.Integer> values) {
        ensureSlotIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, slot_);
        onChanged();
        return this;
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 slot = 2;</code>
       * @return This builder for chaining.
       */
      public Builder clearSlot() {
        slot_ = emptyIntList();
        bitField0_ = (bitField0_ & ~0x00000002);
        onChanged();
        return this;
      }

      private com.google.protobuf.Internal.IntList channel_ = emptyIntList();
      private void ensureChannelIsMutable() {
        if (!((bitField0_ & 0x00000004) != 0)) {
          channel_ = mutableCopy(channel_);
          bitField0_ |= 0x00000004;
         }
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 channel = 3;</code>
       * @return A list containing the channel.
       */
      public java.util.List<java.lang.Integer>
          getChannelList() {
        return ((bitField0_ & 0x00000004) != 0) ?
                 java.util.Collections.unmodifiableList(channel_) : channel_;
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 channel = 3;</code>
       * @return The count of channel.
       */
      public int getChannelCount() {
        return channel_.size();
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 channel = 3;</code>
       * @param index The index of the element to return.
       * @return The channel at the given index.
       */
      public int getChannel(int index) {
        return channel_.getInt(index);
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 channel = 3;</code>
       * @param index The index to set the value at.
       * @param value The channel to set.
       * @return This builder for chaining.
       */
      public Builder setChannel(
          int index, int value) {
        ensureChannelIsMutable();
        channel_.setInt(index, value);
        onChanged();
        return this;
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 channel = 3;</code>
       * @param value The channel to add.
       * @return This builder for chaining.
       */
      public Builder addChannel(int value) {
        ensureChannelIsMutable();
        channel_.addInt(value);
        onChanged();
        return this;
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 channel = 3;</code>
       * @param values The channel to add.
       * @return This builder for chaining.
       */
      public Builder addAllChannel(
          java.lang.Iterable<? extends java.lang.Integer> values) {
        ensureChannelIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, channel_);
        onChanged();
        return this;
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 channel = 3;</code>
       * @return This builder for chaining.
       */
      public Builder clearChannel() {
        channel_ = emptyIntList();
        bitField0_ = (bitField0_ & ~0x00000004);
        onChanged();
        return this;
      }

      private com.google.protobuf.Internal.IntList charge_ = emptyIntList();
      private void ensureChargeIsMutable() {
        if (!((bitField0_ & 0x00000008) != 0)) {
          charge_ = mutableCopy(charge_);
          bitField0_ |= 0x00000008;
         }
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 charge = 4;</code>
       * @return A list containing the charge.
       */
      public java.util.List<java.lang.Integer>
          getChargeList() {
        return ((bitField0_ & 0x00000008) != 0) ?
                 java.util.Collections.unmodifiableList(charge_) : charge_;
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 charge = 4;</code>
       * @return The count of charge.
       */
      public int getChargeCount() {
        return charge_.size();
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 charge = 4;</code>
       * @param index The index of the element to return.
       * @return The charge at the given index.
       */
      public int getCharge(int index) {
        return charge_.getInt(index);
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 charge = 4;</code>
       * @param index The index to set the value at.
       * @param value The charge to set.
       * @return This builder for chaining.
       */
      public Builder setCharge(
          int index, int value) {
        ensureChargeIsMutable();
        charge_.setInt(index, value);
        onChanged();
        return this;
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 charge = 4;</code>
       * @param value The charge to add.
       * @return This builder for chaining.
       */
      public Builder addCharge(int value) {
        ensureChargeIsMutable();
        charge_.addInt(value);
        onChanged();
        return this;
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 charge = 4;</code>
       * @param values The charge to add.
       * @return This builder for chaining.
       */
      public Builder addAllCharge(
          java.lang.Iterable<? extends java.lang.Integer> values) {
        ensureChargeIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, charge_);
        onChanged();
        return this;
      }
      /**
       * <pre>
       * variable length signed int32
       * </pre>
       *
       * <code>repeated sint32 charge = 4;</code>
       * @return This builder for chaining.
       */
      public Builder clearCharge() {
        charge_ = emptyIntList();
        bitField0_ = (bitField0_ & ~0x00000008);
        onChanged();
        return this;
      }

      private com.google.protobuf.Internal.LongList time_ = emptyLongList();
      private void ensureTimeIsMutable() {
        if (!((bitField0_ & 0x00000010) != 0)) {
          time_ = mutableCopy(time_);
          bitField0_ |= 0x00000010;
         }
      }
      /**
       * <pre>
       * variable length signed int64
       * </pre>
       *
       * <code>repeated sint64 time = 5;</code>
       * @return A list containing the time.
       */
      public java.util.List<java.lang.Long>
          getTimeList() {
        return ((bitField0_ & 0x00000010) != 0) ?
                 java.util.Collections.unmodifiableList(time_) : time_;
      }
      /**
       * <pre>
       * variable length signed int64
       * </pre>
       *
       * <code>repeated sint64 time = 5;</code>
       * @return The count of time.
       */
      public int getTimeCount() {
        return time_.size();
      }
      /**
       * <pre>
       * variable length signed int64
       * </pre>
       *
       * <code>repeated sint64 time = 5;</code>
       * @param index The index of the element to return.
       * @return The time at the given index.
       */
      public long getTime(int index) {
        return time_.getLong(index);
      }
      /**
       * <pre>
       * variable length signed int64
       * </pre>
       *
       * <code>repeated sint64 time = 5;</code>
       * @param index The index to set the value at.
       * @param value The time to set.
       * @return This builder for chaining.
       */
      public Builder setTime(
          int index, long value) {
        ensureTimeIsMutable();
        time_.setLong(index, value);
        onChanged();
        return this;
      }
      /**
       * <pre>
       * variable length signed int64
       * </pre>
       *
       * <code>repeated sint64 time = 5;</code>
       * @param value The time to add.
       * @return This builder for chaining.
       */
      public Builder addTime(long value) {
        ensureTimeIsMutable();
        time_.addLong(value);
        onChanged();
        return this;
      }
      /**
       * <pre>
       * variable length signed int64
       * </pre>
       *
       * <code>repeated sint64 time = 5;</code>
       * @param values The time to add.
       * @return This builder for chaining.
       */
      public Builder addAllTime(
          java.lang.Iterable<? extends java.lang.Long> values) {
        ensureTimeIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, time_);
        onChanged();
        return this;
      }
      /**
       * <pre>
       * variable length signed int64
       * </pre>
       *
       * <code>repeated sint64 time = 5;</code>
       * @return This builder for chaining.
       */
      public Builder clearTime() {
        time_ = emptyLongList();
        bitField0_ = (bitField0_ & ~0x00000010);
        onChanged();
        return this;
      }
      @java.lang.Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      @java.lang.Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:AdcHitMap)
    }

    // @@protoc_insertion_point(class_scope:AdcHitMap)
    private static final AdcDataFormat.AdcHitMap DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new AdcDataFormat.AdcHitMap();
    }

    public static AdcDataFormat.AdcHitMap getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<AdcHitMap>
        PARSER = new com.google.protobuf.AbstractParser<AdcHitMap>() {
      @java.lang.Override
      public AdcHitMap parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new AdcHitMap(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<AdcHitMap> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<AdcHitMap> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public AdcDataFormat.AdcHitMap getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_AdcHitMap_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_AdcHitMap_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\023AdcDataFormat.proto\"W\n\tAdcHitMap\022\r\n\005cr" +
      "ate\030\001 \003(\021\022\014\n\004slot\030\002 \003(\021\022\017\n\007channel\030\003 \003(\021" +
      "\022\016\n\006charge\030\004 \003(\021\022\014\n\004time\030\005 \003(\022B\021B\rAdcDat" +
      "aFormatH\001b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_AdcHitMap_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_AdcHitMap_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_AdcHitMap_descriptor,
        new java.lang.String[] { "Crate", "Slot", "Channel", "Charge", "Time", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
