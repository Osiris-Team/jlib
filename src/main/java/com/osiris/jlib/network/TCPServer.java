package com.osiris.jlib.network;

import com.osiris.jlib.network.utils.Future;
import com.osiris.jlib.network.utils.Loop;
import com.osiris.jlib.network.utils.TCPUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class TCPServer {
    public EventLoopGroup bossGroup;
    public EventLoopGroup workerGroup;
    public Channel channel;
    public boolean isEncrypted;
    public Future<Void> isClosing = new Future<Void>().finish(null);
    /**
     * List of connected clients.
     */
    public CopyOnWriteArrayList<TCPClient> clients = new CopyOnWriteArrayList<>();

    /**
     * Uses all the devices threads.
     *
     * @param host        host/address/ip.
     * @param port        port.
     * @param ssl         enable SSL/TLS encryption with a self-signed certificate?
     * @param strictLocal support for running client and server locally, otherwise server
     *                    is unable to receive messages.
     * @throws Exception
     */
    public void open(String host, int port, boolean ssl, boolean strictLocal) throws Exception {
        close();
        //System.out.println(TCPUtils.simpleName(this) + ": open");
        final SslContext sslCtx = ssl ? TCPUtils.buildSslContext() : null;
        isEncrypted = ssl;

        Consumer<Channel> initClientChannel = (ch) -> {
            TCPClient client = new TCPClient(this);
            client.group = workerGroup;
            client.channel = ch;
            client.readers = ch.pipeline();

            if (sslCtx != null) {
                client.readers.addLast(sslCtx.newHandler(ch.alloc()));
            }

            // Input must be created after sslCtx was added
            client.in = new Input(client);
            client.out = new Output(client);

            clients.add(client);
            onClientConnected.accept(client);
        };
        int countThreads = 16;
        try {
            countThreads = Runtime.getRuntime().availableProcessors();
        } catch (Exception e) {
        }
        if (strictLocal) {
            bossGroup = new DefaultEventLoopGroup(1);
            workerGroup = new DefaultEventLoopGroup(countThreads);
        } else {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup(countThreads);
        }
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(strictLocal ? LocalServerChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .childHandler(strictLocal ?
                        new ChannelInitializer<LocalChannel>() {
                            @Override
                            protected void initChannel(LocalChannel ch) throws Exception {
                                initClientChannel.accept(ch);
                            }
                        } :
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                initClientChannel.accept(ch);
                            }
                        });

        // Start the server.
        ChannelFuture future;
        if (strictLocal)
            future = b.bind(new LocalAddress("" + port)).sync();
        else
            future = b.bind(host, port).sync();
        channel = future.channel();
    }

    public Consumer<TCPClient> onClientConnected = client -> {
    };

    public boolean isOpen() {
        return channel != null && channel.isOpen();
    }

    public boolean isClosed() {
        return channel == null || !channel.isOpen();
    }

    public Future<Void> close() throws Exception {
        if(isClosing.isPending()) return isClosing;

        //System.out.println(TCPUtils.simpleName(this) + ": close");
        int clientsToClose = clients.size();
        AtomicInteger clientsClosed = new AtomicInteger();

        if(clientsToClose == 0){
            closeNow();
            return isClosing;
        }

        for (TCPClient c : clients) {
            c.close(false).onSuccess(null_ -> {
                //System.out.println(TCPUtils.simpleName(this) + ": closed above client (^) in server");
                if (clientsClosed.incrementAndGet() >= clientsToClose)
                    closeNow();
            });
        }
        return isClosing;
    }

    /**
     * Same as {@link #close()}, but does not throw Exception,
     * instead throws RuntimeException.
     */
    public Future<Void> close_() {
        try {
            return close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Future<Void> closeNow() {
        //System.out.println(TCPUtils.simpleName(this) + ": closeNow start");
        try{
            if (channel != null) channel.close().sync();
            // Shut down the event loop to terminate all threads.
            if (bossGroup != null) bossGroup.shutdownGracefully().sync();
            if (workerGroup != null) workerGroup.shutdownGracefully().sync();
            //System.out.println(TCPUtils.simpleName(this) + ": closeNow end");
            channel = null;
            bossGroup = null;
            workerGroup = null;
            isClosing.complete(null);
        } catch (Exception ex) {
            isClosing.completeExceptionally(ex);
        }
        return isClosing;
    }
}
