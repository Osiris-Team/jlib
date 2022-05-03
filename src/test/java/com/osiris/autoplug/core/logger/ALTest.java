package com.osiris.autoplug.core.logger;

import org.jline.terminal.TerminalBuilder;
import org.jline.utils.Log;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class ALTest {

    /*
    The test below wont work, because when AnsiConsole.SystemInstall() is called
    in AL.start() that overrides the current System.out streams.
    And thus we cannot see what is written to it.
    @Test
    void testUnsupportedConsole() throws IOException {
        Logger.getLogger("org.jline").setLevel(Level.ALL);
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.ALL);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(out));
        new AL().start();
        AL.info("test");
        String result = out.toString();
        //System.out.println(result);
        assertFalse(result.isEmpty());
        assertFalse(result.contains("\u001B"));
        System.setOut(oldOut);
    }

     */

    @Test
    void test() {
        new AL().start();
        AL.info("Hello!");
        AL.debug(this.getClass(), "Hello debug!");
        AL.warn("Hello warning!");
        AL.warn("Hello warning with ex!", new Exception("Exception messages are awesome!"));
    }

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