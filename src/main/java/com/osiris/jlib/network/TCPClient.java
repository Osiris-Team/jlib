package com.osiris.jlib.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

import java.io.*;
import java.nio.file.Files;
import java.util.function.Consumer;

public class TCPClient implements AutoCloseable {
    public EventLoopGroup group;
    public ChannelFuture future;
    public Channel socket;
    public ChannelPipeline readers;
    public boolean isEncrypted;
    public Output out;
    public Input in;

    /**
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
        group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        // Writes can be directly made, aka data can be directly sent to remote.
        // Problem is waiting for data to return, which means reading stuff.

        // Thus writes and reads are added to a list, which get executed
        // from bottom to top, one after another, however reads do not block
        // the thread because this is done in an event like fashion.

        // writeString
        // readInt
        // writeByte
        TCPClient _this = this;
        Consumer<Channel> initChannel = (ch) -> {
            socket = ch;
            readers = ch.pipeline();
            if (sslCtx != null) {
                readers.addLast(sslCtx.newHandler(ch.alloc(), host, port));
            }

            // Input must be created after sslCtx was added
            in = new Input(_this);
            out = new Output(socket);
        };
        b.group(group)
                .channel(strictLocal ? LocalChannel.class : NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(strictLocal ?
                        new ChannelInitializer<LocalChannel>() {
                            @Override
                            public void initChannel(LocalChannel ch) throws Exception {
                                initChannel.accept(ch);
                            }
                        } :
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                initChannel.accept(ch);
                            }
                        });

        // Start the client.
        if(strictLocal)
            future = b.connect(new LocalAddress(""+port)).sync();
        else
            future = b.connect(host, port).sync();
    }

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
        if(socket!=null) socket.close();
        // Wait until the connection is closed.
        if (future != null) future.channel().closeFuture().sync();
        // Shut down the event loop to terminate all threads.
        if (group != null) group.shutdownGracefully();

        socket = null;
        future = null;
        group = null;
        readers = null;
    }

    public void close_(){
        try{
            close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //
    // Additional methods for reading/writing files.
    //

    /*
    public void writeFile(File file) throws IOException {
        writeFile(file, -1);
    }

    public void readFile(File file) throws IOException {
        readFile(file, -1);
    }

    public void writeFile(File file, long maxBytes) throws IOException {
        long fileSize = file.length();
        if (maxBytes > 0) fileSize = Math.min(fileSize, maxBytes);
        out.writeLong(fileSize);
        long preferedSizeToRead = in.readLong();
        if (preferedSizeToRead > 0) fileSize = Math.min(fileSize, preferedSizeToRead);
        try (InputStream fileIn = Files.newInputStream(file.toPath())) {
            writeStream(fileIn, fileSize);
        }
    }

    public void readFile(File file, long maxBytes) throws IOException {
        long fileSize = in.readLong();
        if (maxBytes > 0) fileSize = Math.min(fileSize, maxBytes);
        out.writeLong(fileSize); // send preferedSizeToRead
        try (OutputStream fileOut = Files.newOutputStream(file.toPath())) {
            readStream(fileOut, fileSize);
        }
    }

    public void writeStream(InputStream in, long maxBytes) throws IOException {
        byte[] buffer = new byte[4096];
        int totalBytesSent = 0;
        int bytesRead;

        while (totalBytesSent < maxBytes && (bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
            totalBytesSent += bytesRead;
        }
    }

    public void readStream(OutputStream out, long maxBytes) throws IOException {
        byte[] buffer = new byte[4096];
        int bytesRead;
        int totalBytesReceived = 0;

        // TODO

        while (totalBytesReceived < maxBytes && (bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
            totalBytesReceived += bytesRead;
        }


    }
    */

}
