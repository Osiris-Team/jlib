package com.osiris.jlib.logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
            Assertions.assertTrue(msg.getType().equals(Message.Type.INFO));
            System.out.println("Executed infoMessageEvent successfully!");
        });

        AL.actionsOnDebugMessageEvent.add(msg -> {
            Assertions.assertTrue(msg.getType().equals(Message.Type.DEBUG));
            System.out.println("Executed debugMessageEvent successfully!");
        });

        AL.actionsOnWarnMessageEvent.add(msg -> {
            Assertions.assertTrue(msg.getType().equals(Message.Type.WARN));
            System.out.println("Executed warnMessageEvent successfully!");
        });

        AL.info("This is an info message!");
        AL.debug(this.getClass(), "This is a debug message!");
        AL.warn("This is a warn message!");
    }

    @Test
    void testFormat() {
        TemporalAccessor temporalAccessor = LocalDateTime.ofInstant(
                Instant.now(), Clock.systemDefaultZone().getZone());

        File dir = new File(System.getProperty("user.dir"));
        File dirYear = new File(dir.getAbsolutePath() + "/"
                + DateTimeFormatter.ofPattern("yyyy", Locale.ENGLISH).format(temporalAccessor));
        File dirMonth = new File(dirYear.getAbsolutePath() + "/"
                + DateTimeFormatter.ofPattern("MM MMMM", Locale.ENGLISH).format(temporalAccessor));
        File dirDay = new File(dirMonth.getAbsolutePath() + "/"
                + DateTimeFormatter.ofPattern("dd EEE", Locale.ENGLISH).format(temporalAccessor));

        File savedLog = new File(dirDay.getAbsolutePath() + "/"
                + DateTimeFormatter.ofPattern("HH-mm-ss  yyyy-MM-dd", Locale.ENGLISH).format(temporalAccessor)
                + "aaaa.log");
        System.out.println(savedLog);
    }

    @Test
    void testMirror() throws IOException {
        File file = new File(System.getProperty("user.dir") + "/test.log");
        File fileErr = new File(System.getProperty("user.dir") + "/test-err.log");
        AL.mirrorSystemStreams(file, fileErr);
        System.out.println("This is out!");
        System.err.println("This is err!");
        System.out.flush();
        System.err.flush();
        assertTrue(file.length() != 0);
        assertTrue(fileErr.length() != 0);
        file.delete();
        fileErr.delete();
    }
}