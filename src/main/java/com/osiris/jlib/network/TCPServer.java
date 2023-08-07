package com.osiris.jlib.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

import java.util.function.Consumer;

public class TCPServer implements AutoCloseable {
    public EventLoopGroup bossGroup;
    public EventLoopGroup workerGroup;
    public ChannelFuture future;
    public boolean isEncrypted;

    /**
     * Uses all the devices threads.
     * @param host        host/address/ip.
     * @param port        port.
     * @param ssl         enable SSL/TLS encryption with a self-signed certificate?
     * @param strictLocal support for running client and server locally, otherwise server
     *                    is unable to receive messages.
     * @throws Exception
     */
    public void open(String host, int port, boolean ssl, boolean strictLocal) throws Exception {
        close();
        final SslContext sslCtx = ssl ? ServerUtil.buildSslContext() : null;
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
            client.out = new Output(client.socket);

            onClientConnected.accept(client);
            System.err.println("Client accepted: " + System.nanoTime());
        };
        int countThreads = 16;
        try{countThreads = Runtime.getRuntime().availableProcessors();} catch (Exception e) {}
        bossGroup = new NioEventLoopGroup(countThreads);
        workerGroup = new NioEventLoopGroup();
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
        if(strictLocal)
            future = b.bind(new LocalAddress(""+port)).sync();
        else
            future = b.bind(host, port).sync();
    }

    public Consumer<TCPClient> onClientConnected = client -> {
    };

    //
    // TODO Ability to gracefully close the connection.
    //

    public boolean isOpen() {
        return future != null;
    }

    public boolean isClosed(){
        return future == null;
    }

    @Override
    public synchronized void close() throws Exception {
        if (future != null) future.channel().close().sync();
        // Shut down the event loop to terminate all threads.
        if (bossGroup != null) bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();

        future = null;
        bossGroup = null;
        workerGroup = null;
    }
}
