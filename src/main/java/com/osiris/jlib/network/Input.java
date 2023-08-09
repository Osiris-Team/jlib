package com.osiris.jlib.network;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Input {
    public TCPClient client;
    /**
     * @see Output#writeID
     */
    protected int readID = 0;
    protected MessageReader<ByteBuf> pendingByteBuf;
    protected MessageReader<String> pendingString;
    protected MessageReader<Boolean> pendingBoolean;
    protected MessageReader<Short> pendingShort;
    protected MessageReader<Integer> pendingInteger;
    protected MessageReader<Long> pendingLong;
    protected MessageReader<Float> pendingFloat;
    protected MessageReader<Double> pendingDouble;
    protected MessageReader<Close> pendingClose;

    /**
     * CHECKLIST for adding a new TYPE to read: <br>
     * 1. Create and add handler in this constructor. <br>
     * 2. Create list that holds completable futures above. <br>
     * 3. Create read method that adds a completable future to that list. <br>
     * 4. Create write method in {@link Output}. <br>
     * 5. Add the list to {@link Output#writeClose(CompletableFuture)}. <br>
     *
     * @param client
     */
    public Input(TCPClient client) {
        this.client = client;
        Consumer<Throwable> onError = e -> {
            // Close the connection when an exception is raised.
            e.printStackTrace();
            try {
                client.close();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
        // ByteBuf
        client.readers.addLast(pendingByteBuf = new MessageReader<>(ByteBuf.class, true, onError));
        // String
        client.readers.addLast(pendingString = new MessageReader<>(String.class, true, onError));
        // Boolean
        client.readers.addLast(pendingBoolean = new MessageReader<>(Boolean.class, true, onError));
        // Short
        client.readers.addLast(pendingShort = new MessageReader<>(Short.class, true, onError));
        // Int
        client.readers.addLast(pendingInteger = new MessageReader<>(Integer.class, true, onError));
        // Long
        client.readers.addLast(pendingLong = new MessageReader<>(Long.class, true, onError));
        // Float
        client.readers.addLast(pendingFloat = new MessageReader<>(Float.class, true, onError));
        // Double
        client.readers.addLast(pendingDouble = new MessageReader<>(Double.class, true, onError));
        // Close
        client.readers.addLast(pendingClose = new MessageReader<>(Close.class, true, onError));
    }

    public CompletableFuture<ByteBuf> readBytes() {
        return pendingByteBuf.read();
    }

    public final CompletableFuture<String> readUTF() {
        return pendingString.read();
    }

    public final CompletableFuture<Boolean> readBoolean() {
        return pendingBoolean.read();
    }

    public final CompletableFuture<Short> readShort() {
        return pendingShort.read();
    }

    public final CompletableFuture<Integer> readInt() {
        return pendingInteger.read();
    }

    public final CompletableFuture<Long> readLong() {
        return pendingLong.read();
    }

    public final CompletableFuture<Float> readFloat() {
        return pendingFloat.read();
    }

    public final CompletableFuture<Double> readDouble() {
        return pendingDouble.read();
    }

    /**
     * See bottom of constructor for details.
     */
    public final CompletableFuture<Close> readClose() {
        return pendingClose.read();
    }
}
