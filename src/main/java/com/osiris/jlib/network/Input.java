package com.osiris.jlib.network;

import com.osiris.jlib.interfaces.BConsumer;
import com.osiris.jlib.network.utils.Future;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class Input implements InputMethods {
    public TCPClient client;
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
        client.readers.addLast(pendingClose = new MessageReader<CloseRequest>(CloseRequest.class, true, onError) {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, CloseRequest msg) throws Exception {
                //System.out.println(TCPUtils.simpleName(client)+"-received: "+msg);
                client.close(true);
            }
        });
    }

    @Override
    public Future<ByteBuf> readBytes() {
        return pendingByteBuf.read();
    }

    @Override
    public final Future<String> readUTF() {
        return pendingString.read();
    }

    @Override
    public final Future<Boolean> readBoolean() {
        return pendingBoolean.read();
    }

    @Override
    public final Future<Short> readShort() {
        return pendingShort.read();
    }

    @Override
    public final Future<Integer> readInt() {
        return pendingInteger.read();
    }

    @Override
    public final Future<Long> readLong() {
        return pendingLong.read();
    }

    @Override
    public final Future<Float> readFloat() {
        return pendingFloat.read();
    }

    @Override
    public final Future<Double> readDouble() {
        return pendingDouble.read();
    }

    @Override
    public final Future<List> readList() {
        return pendingList.read();
    }

    /**
     * @param dir received data gets written to this directory.
     */
    @Override
    public Future<File> readFile(File dir, long maxBytes) {
        Future<File> f = new Future<>();
        readList().onSuccess(l -> {
            long fileSize = (long) l.get(0);
            int sectionSize = (int) l.get(1);
            String fileName = (String) l.get(2);

            long preferedFileSize = fileSize;
            if (maxBytes > 0) preferedFileSize = Math.min(fileSize, maxBytes);
            client.out.writeLong(preferedFileSize); // send preferedSizeToRead
            File file = new File(dir + "/" + fileName);
            try {
                if (fileSize == 0) {
                    f.complete(file);
                    return;
                }
                try (OutputStream fileOut = Files.newOutputStream(file.toPath())) {
                    AtomicLong totalBytesRead = new AtomicLong();
                    readBytes().onSuccess(new BConsumer<ByteBuf>() {
                        @Override
                        public void accept(ByteBuf bytes) throws Exception {
                            fileOut.write(bytes.array());
                            if (totalBytesRead.addAndGet(sectionSize) < fileSize)
                                readBytes().onSuccess(this, f::completeExceptionally);
                            else f.complete(file);
                        }
                    }, f::completeExceptionally);
                }
            } catch (Exception e) {
                f.completeExceptionally(e);
            }
        }, f::completeExceptionally);
        return f;
    }
}
