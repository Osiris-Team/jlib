package com.osiris.jlib.network;

import com.osiris.jlib.network.utils.Future;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Consumer;

public class MessageReader<T> extends SimpleChannelInboundHandler<T> {
    public Queue<Future<T>> pending = new ArrayDeque<>(0);
    public Queue<T> buffer = new ArrayDeque<>(0);
    public Consumer<Throwable> onError;

    public MessageReader(Class<? extends T> inboundMessageType, boolean autoRelease,
                         Consumer<Throwable> onError) {
        super(inboundMessageType, autoRelease);
        this.onError = onError;
    }

    public boolean isEmpty() {
        return pending.isEmpty() && buffer.isEmpty();
    }

    public Future<T> read() {
        Future<T> f = new Future<>();
        if (!buffer.isEmpty()) f.complete(buffer.poll());
        else {
            pending.add(f);
        }
        return f;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {
        //System.out.println(msg);
        if (pending.isEmpty()) buffer.add(msg);
        else {
            pending.poll().complete(msg);
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
}
