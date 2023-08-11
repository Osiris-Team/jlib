package com.osiris.jlib.network;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TCPServerClientTest {
    long bytesSentClient = 0;
    long bytesReceivedServer = 0;

    /**
     * Creates a local server and client,
     * and lets them communicate until both close before returning and closing both.
     * @param codeOnServer
     * @param codeOnClient
     * @return
     */
    public static TCPServer initLocalServerAndClient(BiConsumer<TCPServer, TCPClient> codeOnServer,
                                                     Consumer<TCPClient> codeOnClient) throws Exception {
        TCPServer server = new TCPServer();
        server.onClientConnected = c -> {
            //c.readers.addFirst(new LoggingHandler(LogLevel.INFO));
            codeOnServer.accept(server, c);
        };
        server.open("localhost", 3555, false, true);

        TCPClient client = new TCPClient();
        client.open("localhost", 3555, false, true);
        //client.readers.addFirst(new LoggingHandler(LogLevel.INFO));
        codeOnClient.accept(client);
        boolean serverOpen = true, clientOpen = true;
        for (int i = 0; i < 100; i++) {
            serverOpen = server.isOpen();
            clientOpen = client.isOpen();
            System.out.println("Status: serverOpen="+serverOpen +" clientOpen="+clientOpen);
            if(!serverOpen && !clientOpen) break;
            Thread.sleep(1000);
        }
        if(serverOpen || clientOpen) throw new Exception("Server and/or client are still open after 100 seconds!");
        return server;
    }

    public static boolean isSortedAscendingWith1Step(List<Integer> list) {
        return isSortedAscendingWithSameStep(list, 1);
    }

    public static boolean isSortedAscendingWithSameStep(List<Integer> list, int step) {
        for (int i = 1; i < list.size(); i++) {
            try{
                list.get(i + 1);
            } catch (Exception e) {
                break;
            }
            int expectedValue = list.get(i) + 1;
            int actualValue = list.get(i+1);
            if (expectedValue == list.get(i + 1)) {
                continue;
            } else{
                System.err.println("isSortedAscendingWithSameStep failed at index "+i+" expected "
                +expectedValue+" but got "+actualValue);
                return false;
            }
        }
        return true;
    }

    @Test
    void clientToServer() throws Exception {
        initLocalServerAndClient((s, sc) -> {
            sc.in.readUTF().onSuccess(v -> {
                System.out.println("Received client to server msg: "+v);
                s.close_();
            });
        }, c -> {
            c.out.writeUTF("Hello world!");
            c.close_();
        });
    }

    @Test
    void serverToClient() throws Exception {
        initLocalServerAndClient((s, sc)  -> {
            sc.out.writeUTF("Hello world!");
            s.close_();
        }, c -> {
            c.in.readUTF().onSuccess(s -> {
                System.out.println("Received server to client msg: "+ s);
                c.close_();
            });
        });
    }

    @Test
    void clientToServer10() throws Exception {
        initLocalServerAndClient((s, sc) -> {
            List<Integer> l = new ArrayList<>();
            sc.in.readList().onSuccess(v -> {
                l.addAll(v);
            });
            while (l.size() != 10) Thread.yield();
            assertTrue(isSortedAscendingWith1Step(l));
            s.close_();
        }, c -> {
            List<Integer> l = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                l.add(i);
            }
            c.out.writeList(l);
            c.close_();
        });
    }

    @Test
    void serverToClient10() throws Exception {
        initLocalServerAndClient((s, sc) -> {
            List<Integer> l = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                l.add(i);
            }
            sc.out.writeList(l);
            s.close_();
        }, c -> {
            List<Integer> l = new ArrayList<>();
            c.in.readList().onSuccess(v -> {
                l.addAll(v);
            });
            while (l.size() != 10) Thread.yield();
            assertTrue(isSortedAscendingWith1Step(l));
            c.close_();
        });
    }


    @Test
    void clientToServer100000() throws Exception {
        // TODO when displaying log output the client(server-view) is unable to close
        // and only closes after the 60 second timeout, forcefully.
        initLocalServerAndClient((s, sc) -> {
            List<Integer> l = new ArrayList<>();
            for (int i = 0; i < 100000; i++) {
                sc.in.readInt().thenAccept(v -> {
                    l.add(v);
                });
            }
            s.close_();
            try{ // Check
                for (int i = 0; i < 60; i++) {
                    if(l.size() == 100000) return;
                    Thread.sleep(1000);
                }
                throw new Exception("Failed to receive all 100000 messages within 60 seconds!");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, c -> {
            for (int i = 0; i < 100000; i++) {
                c.out.writeInt(i);
            }
            c.close_();
        });
    }

    @Test
    void serverToClient100000() throws Exception {
        initLocalServerAndClient((s, sc) -> {
            for (int i = 0; i < 100000; i++) {
                sc.out.writeInt(i);
            }
            s.close_();
        }, c -> {
            List<Integer> l = new ArrayList<>();
            for (int i = 0; i < 100000; i++) {
                c.in.readInt().thenAccept(v -> {
                    l.add(v);
                });
            }
            c.close_();
            try{ // Check
                for (int i = 0; i < 60; i++) {
                    if(l.size() == 100000) return;
                    Thread.sleep(1000);
                }
                throw new Exception("Failed to receive all 100000 messages within 60 seconds!");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}