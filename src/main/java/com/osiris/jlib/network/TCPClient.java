package com.osiris.jlib.network;

import com.osiris.jlib.network.utils.Later;
import com.osiris.jlib.network.utils.TCPUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;

import java.util.function.Consumer;

public class TCPClient {
    public EventLoopGroup group;
    public ChannelFuture future;
    public Channel socket;
    public ChannelPipeline readers;
    public boolean isEncrypted;
    public Output out;
    public Input in;
    /**
     * If on the client device this is null. <br>
     * If on the server device this will be the actual {@link TCPServer}
     * instance connected to this {@link TCPClient}.
     */
    public TCPServer server;

    public TCPClient() {
    }

    /**
     *
     * @param server if not null, then this {qli
     *               } is the server-side representation
                      of the actual client, and no actual binding will be done (INTERNAL USE ONLY).
     */
    public TCPClient(TCPServer server) {
        this.server = server;
    }

    /**
     * @param host        host/address/ip.
     * @param port        port.
     * @param ssl         enable SSL/TLS encryption with a self-signed certificate?
     * @param strictLocal support for running client and server locally, otherwise server
     *                    is unable to receive messages.
     * @throws Exception
     */
    public void open(String host, int port, boolean ssl, boolean strictLocal) throws Exception {
        if (server != null) throw new Exception("This TCPClient is a server-side view of the actual" +
                " client and thus cannot call open(...)!");
        close();
        System.out.println(TCPUtils.simpleName(this) + ": open");
        final SslContext sslCtx = ssl ? TCPUtils.buildSslContext() : null;
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
            out = new Output(_this);
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
        if (strictLocal)
            future = b.connect(new LocalAddress("" + port)).sync();
        else
            future = b.connect(host, port).sync();
    }

    public boolean isOpen() {
        return socket != null && socket.isOpen();
    }

    public boolean isClosed() {
        return socket == null || !socket.isOpen();
    }

    public Later<Void> close() throws Exception {
        System.out.println(TCPUtils.simpleName(this) + ": close");
        Later<Void> f = new Later<>();
        if (out != null) out.writeClose(f);
        else {
            closeNow().accept(_null -> {
                f.complete(null);
            });
        }
        return f;
    }

    /**
     * Same as {@link #close()}, but does not throw Exception,
     * instead throws RuntimeException.
     */
    public Later<Void> close_() {
        try {
            return close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes the connection now, without checking remote. <br>
     * This is not recommended, use {@link #close()} or {@link #close_()}
     * instead to close the connection gracefully and ensure all data is transmitted/received.
     */
    public Later<Void> closeNow() throws Exception {
        System.out.println(TCPUtils.simpleName(this) + ": closeNow start");
        Later<Void> f = new Later<>();
        if (socket != null) {
            socket.close().sync();
            //future.cancel(true);
        }
        // Wait until the connection is closed.
        if (future != null) future.channel().closeFuture().sync();
        // This must be done async, since otherwise it blocks indefinitely it seems
        if (group != null && server == null) group.shutdown();

        System.out.println(TCPUtils.simpleName(this) + ": closeNow end");
        socket = null;
        future = null;
        group = null;
        readers = null;
        in = null;
        out = null;
        f.complete(null);
        return f;
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
