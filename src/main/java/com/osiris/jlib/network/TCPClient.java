package com.osiris.jlib.network;

import com.osiris.jlib.network.utils.Future;
import com.osiris.jlib.network.utils.Loop;
import com.osiris.jlib.network.utils.Net;
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
    public Channel channel;
    public ChannelPipeline readers;
    public boolean isEncrypted;
    public Output out;
    public Input in;
    public final Future<Void> isClosing = new Future<>();
    /**
     * If on the client device this is null. <br>
     * If on the server device this will be the actual {@link TCPServer}
     * instance connected to this {@link TCPClient}.
     */
    public TCPServer server;

    public TCPClient() {
    }

    /**
     * @param server if not null, then this {qli
     *               } is the server-side representation
     *               of the actual client, and no actual binding will be done (INTERNAL USE ONLY).
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
    public Future<Void> open(String host, int port, boolean ssl, boolean strictLocal) {
        Future<Void> f = new Future<>();
        if (isOpen() && isClosing.isPending()) {
            // Execute later, if currently closing old connection
            isClosing.onFinish((v, e) -> {
                open(host, port, ssl, strictLocal)
                        .onSuccess(f::complete)
                        .onError(f::completeExceptionally);
            });
            return f;
        }
        if (server != null) {
            f.completeExceptionally(new Exception("This TCPClient is a server-side view of the actual" +
                    " client and thus cannot call open(...)!"));
            return f;
        }
        close(false);
        //System.out.println(TCPUtils.simpleName(this) + ": open");
        final SslContext sslCtx;
        try {
            sslCtx = ssl ? Net.buildSslContext() : null;
        } catch (Exception e) {
            f.completeExceptionally(e);
            return f;
        }
        isEncrypted = ssl;
        group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();


        TCPClient _this = this;
        Consumer<Channel> initChannel = (ch) -> {
            channel = ch;
            readers = ch.pipeline();
            if (sslCtx != null) {
                readers.addLast(sslCtx.newHandler(ch.alloc(), host, port));
            }

            // Input must be created after sslCtx was added
            in = new Input(_this);
            out = new Output(_this);

            /**
             * Close logic:
             * Network errors and exceptions during send/receive
             * cause the connection to close immediately.
             * This is handled in {@link Input}.
             * Close requests from remote are also handled there.
             */
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

        // Start the connection
        try {
            if (strictLocal)
                future = b.connect(new LocalAddress("" + port)).sync();
            else
                future = b.connect(host, port).sync();
        } catch (Exception e) {
            f.completeExceptionally(e);
            return f;
        }

        f.complete(null);
        return f;
    }

    public boolean isOpen() {
        return channel != null;
    }

    public boolean isClosed() {
        return channel == null;
    }

    public Future<Void> close() {
        return close(false);
    }

    public Future<Void> close(boolean isRemoteReadyToClose) {
        if (isClosed()) return isClosing;

        //System.out.println(TCPUtils.simpleName(this) + ": close");
        if (out == null) { // No connection to remote
            closeNow();
            return isClosing;
        }

        // 1
        // Check if we are ready to close
        // We are ready once there are no more pending reads
        Loop.s.add(1, 60, (loop) -> {
            if (isReadyToClose()) {
                loop.isBreak = true;
                if (isClosed()) {
                    return;
                }

                // 2
                // Before closing we want to receive a confirmation
                // from remote, that it's ready to close
                if (isRemoteReadyToClose) closeNow();
                else {
                    try {
                        out.writeCloseRequest();
                    } catch (Exception e) {
                        // Channel might already be closed by remote (how tf does that happen?)
                        return;
                    }
                    Loop.s.add(1, 60, _this -> {
                    }, _this -> {
                        if (isOpen()) {
                            // Failed to close after 60 seconds,
                            // thus closing now!
                            closeNow();
                        }
                    });
                }
            }
        }, loop -> {
            if (!loop.isBreak) {
                // Means that the code above reached the end
                // of 60 seconds and failed to close gracefully
                closeNow();
            }
        });

        return isClosing;
    }

    public Future<Void> close_() {
        return close_(false);
    }

    /**
     * Same as {@link #close()}, but does not throw Exception,
     * instead throws RuntimeException.
     */
    public Future<Void> close_(boolean isRemoteReadyToClose) {
        try {
            return close(isRemoteReadyToClose);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes the connection now, without checking remote. <br>
     * This is not recommended, use {@link #close()} or {@link #close_()}
     * instead to close the connection gracefully and ensure all data is transmitted/received. <br>
     * This is executed later since this method might be called from inside
     * the event loop.
     */
    public Future<Void> closeNow() {
        //System.out.println(TCPUtils.simpleName(this) + ": closeNow start");
        // Problem: this might be called from inside the event loop and thus
        // get in a deadlock, that's why we do the following:
        try {
            if (isOpen()) channel.close().sync();
            if (group != null && server == null) group.shutdownGracefully().sync();

            //System.out.println(TCPUtils.simpleName(this) + ": closeNow end");
            channel = null;
            future = null;
            group = null;
            readers = null;
            in = null;
            out = null;
            isClosing.complete(null);

        } catch (Exception ex) {
            isClosing.completeExceptionally(ex);
        }
        return isClosing;
    }

    public boolean isReadyToClose() {
        if (in == null) return true;
        return in.pendingBoolean.isEmpty() &&
                in.pendingShort.isEmpty() &&
                in.pendingInteger.isEmpty() &&
                in.pendingLong.isEmpty() &&
                in.pendingFloat.isEmpty() &&
                in.pendingDouble.isEmpty() &&
                in.pendingByteBuf.isEmpty() &&
                in.pendingString.isEmpty(); // Do not check pendingClose, since It's always not empty
    }

}
