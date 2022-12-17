/*
 * Copyright Osiris Team
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * AutoPlug License.  Please consult the file "LICENSE" for details.
 */

package com.osiris.jlib.logger;

import com.osiris.jlib.events.MessageEvent;
import org.apache.commons.io.output.TeeOutputStream;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiMode;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The AL (AutoPlugLogger) can be
 * compared to a soviet mailing center.
 * It receives raw messages from the system
 * and forwards them censored or better 'formatted' to the user.
 * The user has multiple ways of getting information (console, log file and online),
 * which all have different capabilities of displaying information.
 * That's why we need this class.
 */
public class AL {
    public static final List<File> listSysOutMirrorFiles = new ArrayList<>();
    /**
     * This {@link PrintWriter} removes the ansi chars before printing out the text. <br>
     * Normally this should be null, unless the current terminal does not support colors. <br>
     */
    public static PrintWriter STRIPPED_OUT;
    public static String NAME;
    public static File DIR;
    public static File DIR_FULL;
    public static File DIR_WARN;
    public static File DIR_ERROR;
    public static File LOG_LATEST;
    public static boolean isDebugEnabled = false;
    public static boolean isStarted = false;
    public static boolean hasAnsiSupport = false;
    public static boolean isForcedAnsi = false;
    // Basically lists that contain code to run when the specific event happens
    public static List<MessageEvent<Message>> actionsOnMessageEvent = new CopyOnWriteArrayList<>();
    public static List<MessageEvent<Message>> actionsOnInfoMessageEvent = new CopyOnWriteArrayList<>();
    public static List<MessageEvent<Message>> actionsOnDebugMessageEvent = new CopyOnWriteArrayList<>();
    public static List<MessageEvent<Message>> actionsOnWarnMessageEvent = new CopyOnWriteArrayList<>();
    public static List<MessageEvent<Message>> actionsOnErrorMessageEvent = new CopyOnWriteArrayList<>();

    public static synchronized void info(String s) {
        final Message msg = new Message(Message.Type.INFO, s);
        final String s1 = MessageFormatter.formatForAnsiConsole(msg);
        final String s2 = MessageFormatter.formatForFile(msg);

        print(s1);
        LogFileWriter.writeToLog(s2);
        actionsOnMessageEvent.forEach(event -> event.executeOnEvent(msg));
        actionsOnInfoMessageEvent.forEach(event -> event.executeOnEvent(msg));
    }

    public static synchronized void debug(Class c, String text) {
        final Message msg = new Message(Message.Type.DEBUG, text);
        msg.setOriginClass(c);
        final String s1 = MessageFormatter.formatForAnsiConsole(msg);
        final String s2 = MessageFormatter.formatForFile(msg);

        if (isDebugEnabled) {
            print(s1);
        }
        LogFileWriter.writeToLog(s2);
        actionsOnMessageEvent.forEach(event -> event.executeOnEvent(msg));
        actionsOnDebugMessageEvent.forEach(event -> event.executeOnEvent(msg));
    }

    public static synchronized void warn(Exception e) {
        warn(null, e, null);
    }

    public static synchronized void warn(String text) {
        warn(null, null, text);
    }

    public static synchronized void warn(String text, Exception e) {
        warn(null, e, text);
    }

    public static synchronized void warn(Exception e, String text) {
        warn(null, e, text);
    }

    public static synchronized void warn(Class c, Exception e) {
        warn(c, e, null);
    }

    public static synchronized void warn(Class c, Exception e, String text) {
        final Message msg = new Message(Message.Type.WARN, text);
        msg.setOriginClass(c);
        msg.setException(e);
        final String s1 = MessageFormatter.formatForAnsiConsole(msg);
        final String s2 = MessageFormatter.formatForFile(msg);

        print(s1);
        LogFileWriter.writeToLog(s2);

        String fileName = "";
        try {
            if (e != null) {
                StackTraceElement[] stacktrace = e.getStackTrace();
                if (stacktrace == null || stacktrace.length == 0) {
                    e = new Exception(e.getMessage() + " (original exception had no stacktrace, thus created a new one to at least see the stacktrace to this warning)");
                    stacktrace = e.getStackTrace();
                }
                fileName = stacktrace[stacktrace.length - 1].getClassName() + " " + stacktrace[0].getClassName() + "." + stacktrace[0].getMethodName();
                if (fileName.length() > 255)
                    fileName = fileName.substring(0, 254);
            } else fileName = "No Exception";
            fileName = fileName.replaceAll("[*<>:?/\"\\|]", "");
            File file = new File(DIR_WARN.getAbsolutePath() + "/" + fileName + ".log");
            if (!file.exists())
                file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(s2);
            writer.flush();
        } catch (Exception exception) {
            exception.printStackTrace();
            System.err.println("Error for file: " + fileName + ".log");
        }
        actionsOnMessageEvent.forEach(event -> event.executeOnEvent(msg));
        actionsOnWarnMessageEvent.forEach(event -> event.executeOnEvent(msg));
    }

    public static synchronized void error(Exception e) {
        error(null, e);
    }

    /**
     * Formats critical error message and closes program after that.
     * This only should be used if program isn't able to continue after this error.
     */
    public static synchronized void error(String errorTitle, Exception e) {
        final Message msg = new Message(Message.Type.ERROR, errorTitle);
        msg.setException(e);
        final String s1 = MessageFormatter.formatForAnsiConsole(msg);
        final String s2 = MessageFormatter.formatForFile(msg);

        print(s1);
        LogFileWriter.writeToLog(s2);

        String fileName = "";
        try {
            if (e != null) {
                StackTraceElement element = e.getStackTrace()[0];
                fileName = element.getClassName() + "()." + element.getMethodName() + "()";
            } else fileName = "No Exception";
            fileName = fileName.replaceAll("[*<>:?/\"\\|]", "");
            File file = new File(DIR_ERROR.getAbsolutePath() + "/" + fileName + ".log");
            if (!file.exists())
                file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(s2);
            writer.flush();
        } catch (Exception exception) {
            exception.printStackTrace();
            System.err.println("Error for file: " + fileName + ".log");
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }

        actionsOnMessageEvent.forEach(event -> event.executeOnEvent(msg));
        actionsOnErrorMessageEvent.forEach(event -> event.executeOnEvent(msg));
        System.exit(0);
    }

    private static synchronized void print(String s) {
        if (hasAnsiSupport)
            System.out.print(s);
        else {
            STRIPPED_OUT.print(s);
            STRIPPED_OUT.flush();
        }
    }


    /**
     * Starts the logger with defaults:
     * name = Logger | config = .../logger-config.yml | loggerDir = .../logs;
     */
    public static void start() {
        start("Logger",
                false,
                new File(System.getProperty("user.dir") + "/logs/latest.log"),
                false
        );
    }

    /**
     * Initialises the AL (AutoPlugLogger).
     * First it checks if debug is enabled, then
     * installs the AnsiConsole and creates the log file.
     * This method can only be called once. Multiple calls won't do anything.
     *
     * @param name      this loggers name.
     * @param debug     should the debug log get displayed. Disabled by default.
     * @param latestLog the file to write the latest log to. The sub-directories /error /warn /full will also
     *                  be created inside that files directory.
     */
    public static void start(String name, boolean debug, File latestLog, boolean forceAnsi) {
        if (isStarted) return;
        isStarted = true;
        if (latestLog.isDirectory()) throw new IllegalArgumentException("Cannot be directory!");
        NAME = name;
        isDebugEnabled = debug;
        isForcedAnsi = forceAnsi;

        try {
            LOG_LATEST = latestLog;
            DIR = latestLog.getParentFile();
            if (!latestLog.getParentFile().exists())
                latestLog.getParentFile().mkdirs();

            // Full logs are saved here (differentiated by creation date).
            DIR_FULL = new File(DIR + "/full");
            if (!DIR_FULL.exists())
                DIR_FULL.mkdirs();

            // Only warnings are saved here (differentiated by class.method).
            DIR_WARN = new File(DIR + "/warn");
            if (!DIR_WARN.exists())
                DIR_WARN.mkdirs();

            // Only errors are saved here (differentiated by class.method).
            DIR_ERROR = new File(DIR + "/error");
            if (!DIR_ERROR.exists())
                DIR_ERROR.mkdirs();

            // If latest_log file from last session exists and has information in it, we first duplicate that file and then replace with new blank file
            try {
                saveLogIfNeeded(LOG_LATEST);
                synchronized (listSysOutMirrorFiles) {
                    for (File file : listSysOutMirrorFiles) {
                        saveLogIfNeeded(file);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (LOG_LATEST.exists()) LOG_LATEST.delete();
            LOG_LATEST.createNewFile();

            //Create writer after file exists
            LogFileWriter.setLogWriterForFile(LOG_LATEST);

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            // Note that if only colors are not supported by the terminal, the below wont throw an Exception.
            // Actually AnsiConsole strips the ANSI away automatically by itself.
            AnsiConsole.systemInstall();
            hasAnsiSupport = true;
        } catch (Exception e) {
            hasAnsiSupport = false;
            AnsiConsole.systemUninstall();
            STRIPPED_OUT = new PrintWriter(LogFileWriter.getBufferedWriterForOutputStream(System.out));
            if (!isForcedAnsi)
                AL.warn("Disabled ANSI/colored output, due to unsupported terminal.", e);
        }

        if (isForcedAnsi) {
            try {
                AnsiConsole.out().setMode(AnsiMode.Default);
                AL.info("Forced terminal to use ANSI.");
            } catch (Exception e) {
                AL.warn("Failed to force terminal to use ANSI.", e);
            }
        }

        AL.debug(AL.class, "Started Logger(" + name + ")");
    }

    private static void saveLogIfNeeded(File logFile) throws IOException {
        if (!logFile.exists() || logFile.length() == 0) return;
        BasicFileAttributes attrs = Files.readAttributes(logFile.toPath(), BasicFileAttributes.class);
        FileTime lastModifiedTime = attrs.lastModifiedTime();
        TemporalAccessor temporalAccessor = LocalDateTime.ofInstant(
                lastModifiedTime.toInstant(), Clock.systemDefaultZone().getZone());

        File dirYear = new File(DIR_FULL.getAbsolutePath() + "/"
                + DateTimeFormatter.ofPattern("yyyy", Locale.ENGLISH).format(temporalAccessor));
        File dirMonth = new File(dirYear.getAbsolutePath() + "/"
                + DateTimeFormatter.ofPattern("MM MMMM", Locale.ENGLISH).format(temporalAccessor));
        File dirDay = new File(dirMonth.getAbsolutePath() + "/"
                + DateTimeFormatter.ofPattern("dd EEE", Locale.ENGLISH).format(temporalAccessor));

        if (!dirDay.exists())
            dirDay.mkdirs();

        File savedLog = new File(dirDay.getAbsolutePath() + "/"
                + DateTimeFormatter.ofPattern("HH-mm-ss  yyyy-MM-dd", Locale.ENGLISH).format(temporalAccessor)
                + "  " + logFile.getName());

        if (!savedLog.exists()) savedLog.createNewFile();

        Files.copy(logFile.toPath(), savedLog.toPath(),
                StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Stops the AL and saves the log to file.
     */
    public static void stop() {
        debug(AL.class, "Stopped " + NAME);
        if (hasAnsiSupport)
            AnsiConsole.systemUninstall();
        LogFileWriter.close();
    }

    /**
     * Mirrors the {@link System#out} and {@link System#err} streams
     * to the provided file, without making them unavailable to the console/terminal. <p>
     * <p>
     * The provided files also get saved into the /full directory with the according formatted name.
     */
    public static void mirrorSystemStreams(File outFile, File errFile) throws IOException {
        if (!isStarted) throw new IllegalStateException("start() must at least have been called once before!");
        if (outFile.isDirectory()) throw new IllegalArgumentException("Cannot be directory!");
        if (errFile.isDirectory()) throw new IllegalArgumentException("Cannot be directory!");

        if (!outFile.exists()) {
            outFile.getParentFile().mkdirs();
            outFile.createNewFile();
        }
        if (!errFile.exists()) {
            errFile.getParentFile().mkdirs();
            errFile.createNewFile();
        }
        saveLogIfNeeded(outFile);
        saveLogIfNeeded(errFile);

        TeeOutputStream teeOut = new TeeOutputStream(System.out, new FileOutputStream(outFile));
        System.setOut(new PrintStream(teeOut));

        TeeOutputStream teeErr = new TeeOutputStream(System.err, new FileOutputStream(errFile));
        System.setErr(new PrintStream(teeErr));

        synchronized (listSysOutMirrorFiles) {
            listSysOutMirrorFiles.add(outFile);
            listSysOutMirrorFiles.add(errFile);
        }
    }

}
