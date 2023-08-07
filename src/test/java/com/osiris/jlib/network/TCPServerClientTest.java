package com.osiris.jlib.network;

import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

class TCPServerClientTest {
    long bytesSentClient = 0;
    long bytesReceivedServer = 0;

    /**
     * Creates a local server and client,
     * and lets them communicate for 10 seconds before returning and closing both.
     * @param codeOnServer
     * @param codeOnClient
     * @return
     */
    public static TCPServer initLocalServerAndClient(Consumer<TCPClient> codeOnServer,
                                                     Consumer<TCPClient> codeOnClient){
        TCPServer server = new TCPServer();
        server.onClientConnected = c -> {
            c.readers.addFirst(new LoggingHandler(LogLevel.INFO));
            codeOnServer.accept(c);
        };
        try {
            server.open("localhost", 3555, false, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try{
            TCPClient client = new TCPClient();
            client.open("localhost", 3555, false, true);
            client.readers.addFirst(new LoggingHandler(LogLevel.INFO));
            codeOnClient.accept(client);
            for (int i = 0; i < 100; i++) { // 10 seconds
                if(server.isClosed() || client.isClosed()) break;
                Thread.sleep(100);
            }
            server.close();
            client.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return server;
    }

    @Test
    void clientToServer() throws Exception {
        initLocalServerAndClient(sc -> {
            sc.in.readUTF().thenAccept(s -> {
                System.out.println("Received client to server msg: "+s);
                sc.close_();
            });
        }, c -> {
            c.out.writeUTF("Hello world!");
        });
    }

    @Test
    void serverToClient() throws Exception {
        initLocalServerAndClient(sc -> {
            sc.out.writeUTF("Hello world!");
        }, c -> {
            c.in.readUTF().thenAccept(s -> {
                System.out.println("Received server to client msg: "+ s);
                c.close_();
            });
        });
    }
}