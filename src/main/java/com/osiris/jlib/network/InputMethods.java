package com.osiris.jlib.network;

import com.osiris.jlib.network.utils.Future;
import io.netty.buffer.ByteBuf;

import java.io.File;
import java.util.List;

public interface InputMethods {
    Future<ByteBuf> readBytes();

    Future<String> readUTF();

    Future<Boolean> readBoolean();

    Future<Short> readShort();

    Future<Integer> readInt();

    Future<Long> readLong();

    Future<Float> readFloat();

    Future<Double> readDouble();

    Future<List> readList();

    /**
     * @param dir received data gets written to this directory.
     */
    default Future<File> readFile(File dir) {
        return readFile(dir, -1);
    }

    Future<File> readFile(File dir, long maxBytes);
}
