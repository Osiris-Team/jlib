package com.osiris.jlib.network;

import com.osiris.jlib.network.utils.Future;
import io.netty.buffer.ByteBuf;

import java.io.File;
import java.util.List;

public interface OutputMethods {
    Future<Void> writeCloseRequest();

    Future<Void> writeBytes(byte[] b);

    Future<Void> writeBytes(ByteBuf b);

    Future<Void> writeUTF(String s);

    Future<Void> writeBoolean(boolean v);

    Future<Void> writeShort(short v);

    Future<Void> writeInt(int v);

    Future<Void> writeLong(long v);

    Future<Void> writeFloat(float v);

    Future<Void> writeDouble(double v);

    <T> Future<Void> writeList(List<T> v);

    default Future<Void> writeFile(File file) {
        return writeFile(file, -1);
    }

    Future<Void> writeFile(File file, long maxBytes);
}
