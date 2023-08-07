package com.osiris.jlib.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Input{
    public TCPClient client;
    protected List<CompletableFuture<ByteBuf>> pendingByteBuf = new ArrayList<>(0);
    protected List<CompletableFuture<String>> pendingString = new ArrayList<>(0);
    protected List<CompletableFuture<Boolean>> pendingBoolean = new ArrayList<>(0);
    protected List<CompletableFuture<Short>> pendingShort = new ArrayList<>(0);
    protected List<CompletableFuture<Integer>> pendingInteger = new ArrayList<>(0);
    protected List<CompletableFuture<Long>> pendingLong = new ArrayList<>(0);
    protected List<CompletableFuture<Float>> pendingFloat = new ArrayList<>(0);
    protected List<CompletableFuture<Double>> pendingDouble = new ArrayList<>(0);

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
        client.readers.addLast(new SimpleChannelInboundHandler<ByteBuf>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                pendingByteBuf.get(0).complete(msg);
                pendingByteBuf.remove(0);
            }
            @Override
            public void channelReadComplete(ChannelHandlerContext ctx) {
                ctx.flush();
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                onError.accept(cause);
            }
        });
        // String
        client.readers.addLast(new SimpleChannelInboundHandler<String>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                pendingString.get(0).complete(msg);
                pendingString.remove(0);
            }
            @Override
            public void channelReadComplete(ChannelHandlerContext ctx) {
                ctx.flush();
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                onError.accept(cause);
            }
        });
        // Boolean
        client.readers.addLast(new SimpleChannelInboundHandler<Boolean>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Boolean msg) throws Exception {
                pendingBoolean.get(0).complete(msg);
                pendingBoolean.remove(0);
            }
            @Override
            public void channelReadComplete(ChannelHandlerContext ctx) {
                ctx.flush();
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                onError.accept(cause);
            }
        });
        // Short
        client.readers.addLast(new SimpleChannelInboundHandler<Short>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Short msg) throws Exception {
                pendingShort.get(0).complete(msg);
                pendingShort.remove(0);
            }
            @Override
            public void channelReadComplete(ChannelHandlerContext ctx) {
                ctx.flush();
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                onError.accept(cause);
            }
        });
        // Int
        client.readers.addLast(new SimpleChannelInboundHandler<Integer>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Integer msg) throws Exception {
                pendingInteger.get(0).complete(msg);
                pendingInteger.remove(0);
            }
            @Override
            public void channelReadComplete(ChannelHandlerContext ctx) {
                ctx.flush();
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                onError.accept(cause);
            }
        });
        // Long
        client.readers.addLast(new SimpleChannelInboundHandler<Long>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Long msg) throws Exception {
                pendingLong.get(0).complete(msg);
                pendingLong.remove(0);
            }
            @Override
            public void channelReadComplete(ChannelHandlerContext ctx) {
                ctx.flush();
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                onError.accept(cause);
            }
        });
        // Float
        client.readers.addLast(new SimpleChannelInboundHandler<Float>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Float msg) throws Exception {
                pendingFloat.get(0).complete(msg);
                pendingFloat.remove(0);
            }
            @Override
            public void channelReadComplete(ChannelHandlerContext ctx) {
                ctx.flush();
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                onError.accept(cause);
            }
        });
        // Double
        client.readers.addLast(new SimpleChannelInboundHandler<Double>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Double msg) throws Exception {
                pendingDouble.get(0).complete(msg);
                pendingDouble.remove(0);
            }
            @Override
            public void channelReadComplete(ChannelHandlerContext ctx) {
                ctx.flush();
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                onError.accept(cause);
            }
        });
    }

    public CompletableFuture<ByteBuf> readBytes()  {
        CompletableFuture<ByteBuf> f = new CompletableFuture<>();
        pendingByteBuf.add(f);
        return f;
    }

    public final CompletableFuture<String> readUTF()  {
        CompletableFuture<String> f = new CompletableFuture<>();
        pendingString.add(f);
        return f;
    }

    public final CompletableFuture<Boolean> readBoolean()  {
        CompletableFuture<Boolean> f = new CompletableFuture<>();
        pendingBoolean.add(f);
        return f;
    }

    public final CompletableFuture<Short> readShort()  {
        CompletableFuture<Short> f = new CompletableFuture<>();
        pendingShort.add(f);
        return f;
    }

    public final CompletableFuture<Integer> readInt()  {
        CompletableFuture<Integer> f = new CompletableFuture<>();
        pendingInteger.add(f);
        return f;
    }

    public final CompletableFuture<Long> readLong()  {
        CompletableFuture<Long> f = new CompletableFuture<>();
        pendingLong.add(f);
        return f;
    }

    public final CompletableFuture<Float> readFloat()  {
        CompletableFuture<Float> f = new CompletableFuture<>();
        pendingFloat.add(f);
        return f;
    }

    public final CompletableFuture<Double> readDouble()  {
        CompletableFuture<Double> f = new CompletableFuture<>();
        pendingDouble.add(f);
        return f;
    }
}
