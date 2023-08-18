package com.osiris.jlib.network;

import com.osiris.jlib.network.utils.Future;
import com.osiris.jlib.network.utils.ListBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelPromise;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

/**
 * Note that this class ís NOT THREAD-SAFE, because
 * we assume that all writes happen on a single thread.
 */
public class OutputOrdered implements OutputMethods {
    public TCPClient client;
    /**
     * Gets incremented on every write, and is used to uniquely
     * identify a write operation. <br>
     * This makes it possible to send and receive data in an orderly, but
     * still async fashion. <br>
     * Remote expects a read with the same ID ({@link InputOrdered#readID}).
     */
    protected int writeID = 0;

    public OutputOrdered(TCPClient client) {
        this.client = client;
    }

    public <T> Future<Void> writeAndFlushObject(T v) {
        synchronized (this) {
            writeID++;
            Future<Void> onRemoteReceived = new Future<>();
            ChannelPromise p = client.channel.newPromise();
            p.addListener(onRemoteReceived::finishWithObject);
            client.group.execute(() -> {
                client.channel.writeAndFlush(new Data<>(writeID, v), p);
            });
            return onRemoteReceived;
        }
    }

    /**
     * Writes a close request, and expects a close request response from remote
     * to confirm and execute the close. <br>
     * If no confirmation is received within 60 seconds,
     * the connection will be closed anyway.
     */
    @Override
    public Future<Void> writeCloseRequest() {
        return writeAndFlushObject(new CloseRequest());
    }

    /**
     * Write a/multiple bytes, aka raw data.
     */
    @Override
    public Future<Void> writeBytes(byte[] b) {
        return writeAndFlushObject(b);

    }

    /**
     * Write a/multiple bytes, aka raw data.
     */
    @Override
    public Future<Void> writeBytes(ByteBuf b) {
        return writeAndFlushObject(b);

    }

    /**
     * Write a string.
     */
    @Override
    public Future<Void> writeUTF(String s) {
        return writeAndFlushObject(s);
    }


    /**
     * Writes a {@code boolean} to the underlying output stream as
     * a 1-byte value. The value {@code true} is written out as the
     * value {@code (byte)1}; the value {@code false} is
     * written out as the value {@code (byte)0}. If no exception is
     * thrown, the counter {@code written} is incremented by
     * {@code 1}.
     *
     * @param v a {@code boolean} value to be written.
     */
    @Override
    public final Future<Void> writeBoolean(boolean v) {
        return writeAndFlushObject(v);
    }

    /**
     * Writes a {@code short} to the underlying output stream as two
     * bytes, high byte first. If no exception is thrown, the counter
     * {@code written} is incremented by {@code 2}.
     *
     * @param v a {@code short} to be written.
     */
    @Override
    public final Future<Void> writeShort(short v) {
        return writeAndFlushObject(v);
    }

    /**
     * Writes an {@code int} to the underlying output stream as four
     * bytes, high byte first. If no exception is thrown, the counter
     * {@code written} is incremented by {@code 4}.
     *
     * @param v an {@code int} to be written.
     */
    @Override
    public final Future<Void> writeInt(int v) {
        return writeAndFlushObject(v);
    }

    /**
     * Writes a {@code long} to the underlying output stream as eight
     * bytes, high byte first. In no exception is thrown, the counter
     * {@code written} is incremented by {@code 8}.
     *
     * @param v a {@code long} to be written.
     */
    @Override
    public final Future<Void> writeLong(long v) {
        return writeAndFlushObject(v);
    }

    /**
     * Converts the float argument to an {@code int} using the
     * {@code floatToIntBits} method in class {@code Float},
     * and then writes that {@code int} value to the underlying
     * output stream as a 4-byte quantity, high byte first. If no
     * exception is thrown, the counter {@code written} is
     * incremented by {@code 4}.
     *
     * @param v a {@code float} value to be written.
     * @see Float#floatToIntBits(float)
     */
    @Override
    public final Future<Void> writeFloat(float v) {
        return writeAndFlushObject(v);
    }

    /**
     * Converts the double argument to a {@code long} using the
     * {@code doubleToLongBits} method in class {@code Double},
     * and then writes that {@code long} value to the underlying
     * output stream as an 8-byte quantity, high byte first. If no
     * exception is thrown, the counter {@code written} is
     * incremented by {@code 8}.
     *
     * @param v a {@code double} value to be written.
     * @see Double#doubleToLongBits(double)
     */
    @Override
    public final Future<Void> writeDouble(double v) {
        return writeAndFlushObject(v);
    }

    @Override
    public final <T> Future<Void> writeList(List<T> v) {
        return writeAndFlushObject(v);
    }

    @Override
    public Future<Void> writeFile(File file, long maxBytes) {
        Future<Void> future = new Future<>();
        long fileSizeBytes = file.length();
        if (maxBytes > 0) fileSizeBytes = Math.min(fileSizeBytes, maxBytes);
        int sectionSizeBytes = 65000;
        long finalFileSize = fileSizeBytes;

        Future<Long> preferedSize = new Future<>();
        /*
        writeList(...)
        .readLong(preferedSize)
        .await();
        .async(...)
        .await()
        .writeBytes(sectionsCount, i -> {
            // Loop code
            byte[] buffer = new byte[sectionSizeBytes];
            return buffer;
        })
         */

        // ProtocolConstructor

        writeList(new ListBuilder()
                .add(fileSizeBytes)
                .add(sectionSizeBytes)
                .add(file.getName())
                .list).onSuccess(null_ -> {
            client.in.readLong().onSuccess(preferedSizeToSend -> {
                if (preferedSizeToSend > 0) preferedSizeToSend = Math.min(finalFileSize, preferedSizeToSend);
                else preferedSizeToSend = finalFileSize;
                long sentBytes = 0;
                try (InputStream fileIn = Files.newInputStream(file.toPath())) {
                    byte[] buffer = new byte[sectionSizeBytes];
                    int bytesRead = 0;
                    while (sentBytes < preferedSizeToSend &&
                            (bytesRead = fileIn.read(buffer)) != -1) {
                        writeBytes(buffer);
                        sentBytes += bytesRead;
                    }
                }
            }, future::completeExceptionally);
        }, future::completeExceptionally);
        return future;
    }

}
