package com.osiris.jlib.network;

import com.osiris.jlib.network.utils.Future;
import com.osiris.jlib.network.utils.TCPUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;
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
    protected MessageReader<List> pendingList;
    protected MessageReader<CloseRequest> pendingClose;

    /**
     * CHECKLIST for adding a new TYPE to read: <br>
     * 1. Create and add handler in this constructor. <br>
     * 2. Create list that holds completable futures above. <br>
     * 3. Create read method that adds a completable future to that list. <br>
     * 4. Create write method in {@link Output}. <br>
     * 5. Add the list to {@link Output#writeCloseRequest()}. <br>
     *
     * @param client
     */
    public Input(TCPClient client) {
        this.client = client;
        Consumer<Throwable> onError = e -> {
            // Close the connection when an exception is raised.
            e.printStackTrace();
            try {
                client.close(true);
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
        // List
        client.readers.addLast(pendingList = new MessageReader<>(List.class, true, onError));
        // Close
        client.readers.addLast(pendingClose = new MessageReader<CloseRequest>(CloseRequest.class, true, onError){
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, CloseRequest msg) throws Exception {
                //System.out.println(TCPUtils.simpleName(client)+"-received: "+msg);
                client.close(true);
            }
        });
    }

    public Future<ByteBuf> readBytes() {
        return pendingByteBuf.read();
    }

    public final Future<String> readUTF() {
        return pendingString.read();
    }

    public final Future<Boolean> readBoolean() {
        return pendingBoolean.read();
    }

    public final Future<Short> readShort() {
        return pendingShort.read();
    }

    public final Future<Integer> readInt() {
        return pendingInteger.read();
    }

    public final Future<Long> readLong() {
        return pendingLong.read();
    }

    public final Future<Float> readFloat() {
        return pendingFloat.read();
    }

    public final Future<Double> readDouble() {
        return pendingDouble.read();
    }

    public final Future<List> readList(){
        return pendingList.read();
    }
}