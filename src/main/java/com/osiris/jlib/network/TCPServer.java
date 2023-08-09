package com.osiris.jlib.network;

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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class TCPServer {
    public EventLoopGroup bossGroup;
    public EventLoopGroup workerGroup;
    public Channel socket;
    public boolean isEncrypted;
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
        System.out.println(TCPUtils.simpleName(this) + ": open");
        close();
        final SslContext sslCtx = ssl ? TCPUtils.buildSslContext() : null;
        isEncrypted = ssl;

        // Writes can be directly made, aka data can be directly sent to remote.
        // Problem is waiting for data to return, which means reading stuff.

        // Thus writes and reads are added to a list, which get executed
        // from bottom to top, one after another, however reads do not block
        // the thread because this is done in an event like fashion.

        // writeString
        // readInt
        // writeByte
        Consumer<Channel> initClientChannel = (ch) -> {
            TCPClient client = new TCPClient();
            client.socket = ch;
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
        socket = future.channel();
    }

    public Consumer<TCPClient> onClientConnected = client -> {
    };

    public boolean isOpen() {
        return socket != null && socket.isOpen();
    }

    public boolean isClosed() {
        return socket == null || !socket.isOpen();
    }

    public CompletableFuture<Void> close() throws Exception {
        System.out.println(TCPUtils.simpleName(this) + ": close");
        CompletableFuture<Void> f = new CompletableFuture<>();
        int clientsToClose = clients.size();
        AtomicInteger clientsClosed = new AtomicInteger();

        for (TCPClient c : clients) {

            c.close().thenAccept(null_ -> {
                System.out.println(TCPUtils.simpleName(this) + ": closed above client (^) in server");
                if (clientsClosed.incrementAndGet() >= clientsToClose)
                    try {
                        closeNow().thenAccept(_null -> {
                            f.complete(null);
                        });
                    } catch (Exception e) {
                        f.completeExceptionally(e);
                    }
            });
        }
        return f;
    }

    /**
     * Same as {@link #close()}, but does not throw Exception,
     * instead throws RuntimeException.
     */
    public CompletableFuture<Void> close_() {
        try {
            return close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Void> closeNow() throws Exception {
        CompletableFuture<Void> f = new CompletableFuture<>();
        System.out.println(TCPUtils.simpleName(this) + ": closeNow start");
        if (socket != null) {
            socket.close().sync();
            //future.cancel(true);
        }
        // Shut down the event loop to terminate all threads.
        if (bossGroup != null) bossGroup.shutdownGracefully().sync();
        // This must be done async, since otherwise it blocks indefinitely it seems
        if (workerGroup != null) workerGroup.shutdownGracefully().addListener(e -> {
            System.out.println(TCPUtils.simpleName(this) + ": closeNow end");
            socket = null;
            bossGroup = null;
            workerGroup = null;
        });
        return f;
    }
}
