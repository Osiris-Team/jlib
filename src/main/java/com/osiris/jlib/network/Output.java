package com.osiris.jlib.network;

import com.osiris.jlib.network.utils.Loop;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Note that this class ís NOT THREAD-SAFE, because
 * we assume that all writes happen on a single thread.
 */
public class Output {
    public TCPClient client;
    public Channel socket;
    /**
     * Gets incremented on every write, and is used to uniquely
     * identify a write operation. <br>
     * This makes it possible to send and receive data in an orderly, but
     * still async fashion. <br>
     * Remote expects a read with the same ID ({@link Input#readID}).
     */
    protected int writeID = 0;

    public Output(TCPClient client) {
        this.client = client;
        this.socket = client.socket;
    }

    // TODO
    public <T> void write(T v) {
        //Data<T> d = new Data<>(writeID++, v);
        //socket.writeAndFlush(d);
        /*
        ByteBuf buf = Unpooled.buffer(32);
        buf.writeInt(writeID++);
        buf.wri
        socket.writeAndFlush(buf);

         */
    }

    /**
     * Writes a close request, and expects a close request response from remote
     * to confirm and execute the close. <br>
     * If no confirmation is received within 60 seconds,
     * the connection will be closed anyway.
     *
     * @param code code to run once close was read by remote.
     */
    public void writeClose(CompletableFuture<?> code) {
        client.in.readClose().thenAccept(v -> {
            // Got close from remote telling that it's ready to close
            try {
                client.closeNow();
                code.complete(null);
            } catch (Exception e) {
                code.completeExceptionally(e);
            }
        });
        // Only write the close once there is no more pending reads
        Loop.main.add(1, 60, (_this) -> {
            if (client.in.pendingBoolean.isEmpty() &&
                    client.in.pendingShort.isEmpty() &&
                    client.in.pendingInteger.isEmpty() &&
                    client.in.pendingLong.isEmpty() &&
                    client.in.pendingFloat.isEmpty() &&
                    client.in.pendingDouble.isEmpty() &&
                    client.in.pendingByteBuf.isEmpty() &&
                    client.in.pendingString.isEmpty()) { // Do not check pendingClose, since It's always not empty
                socket.write(new Close());
                socket.flush();
                _this.isBreak = true;
            }
        });
    }

    private <T> void addLastPendingReadIfNeeded(List<CompletableFuture<T>> pendingReads,
                                                List<CompletableFuture<?>> all) {
        if (!pendingReads.isEmpty()) {
            all.add(pendingReads.get(pendingReads.size() - 1));
        }
    }

    /**
     * Write a/multiple bytes, aka raw data.
     */
    public void writeBytes(byte[] b) {
        socket.write(b);
        flush();
    }

    /**
     * Write a/multiple bytes, aka raw data.
     */
    public void writeBytes(ByteBuf b) {
        socket.write(b);
        flush();
    }

    /**
     * Submit/Send the buffered data.
     */
    public void flush() {
        socket.flush();
    }

    /**
     * Write a string.
     */
    public void writeUTF(String s) {
        socket.write(s);
        flush();
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
    public final void writeBoolean(boolean v) {
        socket.write(v);
        flush();
    }

    /**
     * Writes a {@code short} to the underlying output stream as two
     * bytes, high byte first. If no exception is thrown, the counter
     * {@code written} is incremented by {@code 2}.
     *
     * @param v a {@code short} to be written.
     */
    public final void writeShort(short v) {
        socket.write(v);
        flush();
    }

    /**
     * Writes an {@code int} to the underlying output stream as four
     * bytes, high byte first. If no exception is thrown, the counter
     * {@code written} is incremented by {@code 4}.
     *
     * @param v an {@code int} to be written.
     */
    public final void writeInt(int v) {
        socket.write(v);
        flush();
    }

    /**
     * Writes a {@code long} to the underlying output stream as eight
     * bytes, high byte first. In no exception is thrown, the counter
     * {@code written} is incremented by {@code 8}.
     *
     * @param v a {@code long} to be written.
     */
    public final void writeLong(long v) {
        socket.write(v);
        flush();
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
     * @see java.lang.Float#floatToIntBits(float)
     */
    public final void writeFloat(float v) {
        socket.write(v);
        flush();
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
     * @see java.lang.Double#doubleToLongBits(double)
     */
    public final void writeDouble(double v) {
        socket.write(v);
        flush();
    }
}
