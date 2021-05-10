package com.osiris.autoplug.core.logger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ALTest {

    @Test
    void testOnMessageEvents() {
        new AL().start();

        AL.actionsOnInfoMessageEvent.add(msg -> {
           assertTrue(msg.getType().equals(Message.Type.INFO));
           System.out.println("Executed infoMessageEvent successfully!");
        });

        AL.actionsOnDebugMessageEvent.add(msg -> {
            assertTrue(msg.getType().equals(Message.Type.DEBUG));
            System.out.println("Executed debugMessageEvent successfully!");
        });

        AL.actionsOnWarnMessageEvent.add(msg -> {
            assertTrue(msg.getType().equals(Message.Type.WARN));
            System.out.println("Executed warnMessageEvent successfully!");
        });

        AL.info("This is an info message!");
        AL.debug(this.getClass(), "This is a debug message!");
        AL.warn("This is a warn message!");
    }
}