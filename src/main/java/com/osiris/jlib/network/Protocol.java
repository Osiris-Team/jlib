package com.osiris.jlib.network;

import io.netty.buffer.ByteBuf;

import java.io.File;
import java.util.List;

public class Protocol {
    private final int totalReads = 0;
    private final int totalWrites = 0;


    public Protocol readBytes() {
        return this;
    }


    public Protocol readUTF() {
        return this;
    }


    public Protocol readBoolean() {
        return this;
    }


    public Protocol readShort() {
        return this;
    }


    public Protocol readInt() {
        return this;
    }


    public Protocol readLong() {
        return this;
    }


    public Protocol readFloat() {
        return this;
    }


    public Protocol readDouble() {
        return this;
    }


    public Protocol readList() {
        return this;
    }


    public Protocol readFile(File dir, long maxBytes) {
        return this;
    }


    public Protocol writeBytes(byte[] b) {
        return this;
    }


    public Protocol writeBytes(ByteBuf b) {
        return this;
    }


    public Protocol writeUTF(String s) {
        return this;
    }


    public Protocol writeBoolean(boolean v) {
        return this;
    }


    public Protocol writeShort(short v) {
        return this;
    }


    public Protocol writeInt(int v) {
        return this;
    }


    public Protocol writeLong(long v) {
        return this;
    }


    public Protocol writeFloat(float v) {
        return this;
    }


    public Protocol writeDouble(double v) {
        return this;
    }


    public <T> Protocol writeList(List<T> v) {
        return this;
    }


    public Protocol writeFile(File file, long maxBytes) {
        return this;
    }
}
