package com.osiris.jlib.network;

import com.osiris.jlib.network.utils.Future;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.TypeParameterMatcher;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class InputOrdered extends SimpleChannelInboundHandler<Data> implements InputMethods {
    /**
     * Pending read operations that did not consume the data yet. <br>
     * key: data id, value: future that gets completed once data received.
     */
    public Map<Integer, Future<?>> pendingReads = new HashMap<>();
    /**
     * Received data that was not consumed by a read operation yet. <br>
     * key: data id, value: data
     */
    public Map<Integer, Data<?>> dataBuffer = new HashMap<>();
    public Consumer<Throwable> onError;
    protected TypeParameterMatcher closeMatcher = TypeParameterMatcher.get(CloseRequest.class);

    /**
     * {@link OutputOrdered#writeID}.
     */
    protected int readID = 0;
    public TCPClient client;

    public InputOrdered(TCPClient client, boolean autoRelease,
                        Consumer<Throwable> onError) {
        super(Data.class, autoRelease);
        this.client = client;
        this.onError = onError;
    }

    public boolean isEmpty() {
        return pendingReads.isEmpty() && dataBuffer.isEmpty();
    }

    /**
     * Gets triggered when data is requested from remote, by this device. <br>
     * Goes hand in hand with {@link #channelRead0(ChannelHandlerContext, Data)}.
     */
    private void read(Future<?> f) {
        synchronized (this) {
            readID++;
            if (!dataBuffer.isEmpty()) {
                Data<?> data = dataBuffer.get(readID);
                if (data == null) {
                    // Buffer does not contain data for this read yet
                    pendingReads.put(readID, f);
                } else {
                    f.finishWithObject(data.object);
                }
            } else {
                pendingReads.put(readID, f);
            }
        }
    }

    /**
     * Gets triggered when data is received from remote. <br>
     * Goes hand in hand with {@link #read(Future)}.
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Data data) throws Exception {
        synchronized (this) {
            // Close
            if (closeMatcher.match(data.object)) {
                client.close(true);
                return;
            }

            if (pendingReads.isEmpty()) dataBuffer.put(data.id, data);
            else {
                Future<?> future = pendingReads.get(data.id);
                if (future == null) {
                    // Pending reads does not contain this yet
                    dataBuffer.put(data.id, data);
                } else {
                    future.finishWithObject(data.object);
                }
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        onError.accept(cause);
    }

    @Override
    public Future<ByteBuf> readBytes() {
        Future<ByteBuf> f = new Future<>();
        read(f);
        return f;
    }

    @Override
    public Future<String> readUTF() {
        Future<String> f = new Future<>();
        read(f);
        return f;
    }

    @Override
    public Future<Boolean> readBoolean() {
        Future<Boolean> f = new Future<>();
        read(f);
        return f;
    }

    @Override
    public Future<Short> readShort() {
        Future<Short> f = new Future<>();
        read(f);
        return f;
    }

    @Override
    public Future<Integer> readInt() {
        Future<Integer> f = new Future<>();
        read(f);
        return f;
    }

    @Override
    public Future<Long> readLong() {
        Future<Long> f = new Future<>();
        read(f);
        return f;
    }

    @Override
    public Future<Float> readFloat() {
        Future<Float> f = new Future<>();
        read(f);
        return f;
    }

    @Override
    public Future<Double> readDouble() {
        Future<Double> f = new Future<>();
        read(f);
        return f;
    }

    @Override
    public Future<List> readList() {
        Future<List> f = new Future<>();
        read(f);
        return f;
    }

    @Override
    public Future<File> readFile(File dir, long maxBytes) {
        // TODO implement logic/protocol
        Future<File> f = new Future<>();
        read(f);
        return f;
    }
}
