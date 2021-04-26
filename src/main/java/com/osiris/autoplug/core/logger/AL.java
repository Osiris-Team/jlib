/*
 * Copyright Osiris Team
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * AutoPlug License.  Please consult the file "LICENSE" for details.
 */

package com.osiris.autoplug.core.logger;

import com.osiris.dyml.DYModule;
import com.osiris.dyml.DreamYaml;
import com.osiris.dyml.watcher.DYAction;
import com.osiris.dyml.watcher.DYWatcher;
import jdk.internal.org.jline.reader.LineReader;
import jdk.internal.org.jline.reader.LineReaderBuilder;
import org.fusesource.jansi.AnsiConsole;
import org.w3c.dom.Attr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    public static String NAME;
    public static File DIR;
    public static File DIR_FULL;
    public static File DIR_WARN;
    public static File DIR_ERROR;
    public static File LOG_LATEST;
    public static boolean isDebugEnabled = false;
    public static boolean isStarted = false;


    /**
     * Starts the logger with defaults:
     * name = Logger | config = .../logger-config.yml | loggerDir = .../logs;
     */
    public void start(){
        start("Logger",
                new DreamYaml(System.getProperty("user.dir")+"/logger-config.yml"),
                new File(System.getProperty("user.dir")+"/logs")
                );
    }

    /**
     * Initialises the AL (AutoPlugLogger).
     * First it checks if debug is enabled, then
     * installs the AnsiConsole and creates the log file.
     * This method can only be called once. Multiple calls won't do anything.
     * @param name this loggers name.
     * @param loggerConfig the logger config
     * @param loggerDir the directory where logs should be stored
     */
    public void start(String name, DreamYaml loggerConfig, File loggerDir){
        if (isStarted) return;
        isStarted = true;
        NAME = name;
        try {
            // First check if the debug option is enabled. This has to be done like this, because the GeneralConfig class uses this Logger to display data.
            loggerConfig.load();
            DYModule debug = loggerConfig.add("debug").setDefValue("false");
            loggerConfig.save();
            isDebugEnabled = debug.asBoolean();

            DYWatcher watcher = new DYWatcher(false);
            watcher.start();
            watcher.addYaml(loggerConfig);

            DYAction action = new DYAction(loggerConfig);
            action.setRunnable(()->{
                if(action.getEventKind().equals(StandardWatchEventKinds.ENTRY_MODIFY))
                    try{
                        action.getYaml().reload();
                        isDebugEnabled = debug.asBoolean();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
            });
            watcher.addAction(action);

            DIR = loggerDir;
            if (!loggerDir.exists())
                loggerDir.mkdirs();

            // Full logs are saved here (differentiated by creation date).
            DIR_FULL = new File(loggerDir.getAbsolutePath()+"/full");
            if (!DIR_FULL.exists())
                DIR_FULL.mkdirs();

            // Only warnings are saved here (differentiated by class.method).
            DIR_WARN = new File(loggerDir.getAbsolutePath()+"/warn");
            if (!DIR_WARN.exists())
                DIR_WARN.mkdirs();

            // Only errors are saved here (differentiated by class.method).
            DIR_ERROR = new File(loggerDir.getAbsolutePath()+"/error");
            if (!DIR_ERROR.exists())
                DIR_ERROR.mkdirs();

            LOG_LATEST = new File(DIR_FULL.getAbsolutePath()+"/latest.log");;

            // If latest_log file from last session exists and has information in it, we first duplicate that file and then replace with new blank file
            try{
                if (LOG_LATEST.exists() && LOG_LATEST.length()!=0){
                    // Gets the last modified date and saves it to a new file
                    BasicFileAttributes attrs = Files.readAttributes(LOG_LATEST.toPath(), BasicFileAttributes.class);
                    FileTime time = attrs.lastModifiedTime();

                    File savedLog = new File(DIR_FULL.getAbsolutePath()+ "/"
                            + DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss").format(
                                    LocalDateTime.ofInstant(
                                            time.toInstant(), Clock.systemDefaultZone().getZone()))+".log");

                    if (!savedLog.exists()) savedLog.createNewFile();

                    Files.copy(LOG_LATEST.toPath(), savedLog.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (LOG_LATEST.exists()) LOG_LATEST.delete();
            LOG_LATEST.createNewFile();

            //Create writer after file exists
            LogFileWriter.createLogWriter(LOG_LATEST);

            debug(this.getClass(),"Started Logger("+name+")");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops the AL and saves the log to file.
     */
    public void stop(){
        debug(this.getClass(),"Stopped "+NAME);
        AnsiConsole.systemUninstall();

        if (LOG_LATEST.exists())
            try {
                File savedLog = new File(DIR_FULL.getAbsolutePath()+"/"+MessageFormatter.dtf_long.format(LocalDateTime.now())+".log");
                if (!savedLog.exists()) savedLog.createNewFile();

                Files.copy(LOG_LATEST.toPath(), savedLog.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);

                LogFileWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }


    public static synchronized void info(String s) {
        final Message msg = new Message(MessageType.INFO, s);
        final String s1 = MessageFormatter.formatForAnsiConsole(msg);
        final String s2 = MessageFormatter.formatForFile(msg);

        output(s1);
        LogFileWriter.writeToLog(s2);
    }

    public static synchronized void debug(Class c, String text) {
        final Message msg = new Message(MessageType.DEBUG, text);
        msg.setOriginClass(c);
        final String s1 = MessageFormatter.formatForAnsiConsole(msg);
        final String s2 = MessageFormatter.formatForFile(msg);

        if (isDebugEnabled) {
            output(s1);
        }
        LogFileWriter.writeToLog(s2);
    }

    public static synchronized void warn(Exception e){
        warn(null, e, null);
    }

    public static synchronized void warn (String text){
        warn(null, null, text);
    }

    public static synchronized void warn (String text, Exception e){
        warn(null, e, text);
    }

    public static synchronized void warn (Exception e, String text){
        warn(null, e, text);
    }

    public static synchronized void warn (Class c, Exception e){
        warn(c, e, null);
    }

    public static synchronized void warn(Class c, Exception e, String text){
        final Message msg = new Message(MessageType.WARN, text);
        msg.setOriginClass(c);
        msg.setException(e);
        final String s1 = MessageFormatter.formatForAnsiConsole(msg);
        final String s2 = MessageFormatter.formatForFile(msg);

        output(s1);
        LogFileWriter.writeToLog(s2);

        String fileName = "";
        try{
            if (e!=null){
                StackTraceElement element = e.getStackTrace()[0];
                fileName = element.getClassName()+"()."+element.getMethodName()+"()";
            }
            else fileName = "No Exception";
            fileName = fileName.replaceAll("[*<>:?/\"\\|]","");
            File file = new File(DIR_WARN.getAbsolutePath()+"/"+fileName+".log");
            if (!file.exists())
                file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(s2);
            writer.flush();
        } catch (Exception exception) {
            exception.printStackTrace();
            System.err.println("Error for file: "+fileName+".log");
        }
    }

    public static synchronized void error(Exception e){
        error(null, e);
    }

    /**
     * Formats critical error message and closes program after that.
     * This only should be used if program isn't able to continue after this error.
     */
    public static synchronized void error(String errorTitle, Exception e) {

        final Message msg = new Message(MessageType.ERROR, errorTitle);
        msg.setException(e);
        final String s1 = MessageFormatter.formatForAnsiConsole(msg);
        final String s2 = MessageFormatter.formatForFile(msg);

        output(s1);
        LogFileWriter.writeToLog(s2);

        String fileName = "";
        try{
            if (e!=null){
                StackTraceElement element = e.getStackTrace()[0];
                fileName = element.getClassName()+"()."+element.getMethodName()+"()";
            }
            else fileName = "No Exception";
            fileName = fileName.replaceAll("[*<>:?/\"\\|]","");
            File file = new File(DIR_ERROR.getAbsolutePath()+"/"+fileName+".log");
            if (!file.exists())
                file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(s2);
            writer.flush();
        } catch (Exception exception) {
            exception.printStackTrace();
            System.err.println("Error for file: "+fileName+".log");
        }

        try{
            Thread.sleep(10000);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }

        System.exit(0);
    }

    private static synchronized void output(String s){
        System.out.print(s);
    }

}
