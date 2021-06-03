/*
 * Copyright Osiris Team
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * AutoPlug License.  Please consult the file "LICENSE" for details.
 */

package com.osiris.autoplug.core.logger;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiPrintStream;
import org.fusesource.jansi.io.AnsiOutputStream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class LogFileWriter {
    public static File logFile;
    private static BufferedWriter bw;

    public static void createLogWriter(File file) {
        logFile = file;
        try {
            bw = getBufferedWriterForFile(logFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * ONLY FOR JANSI BELOW v2! <br>
     * Returns a new {@link BufferedWriter} for the given file.
     * This writer is ANSI free, thus perfectly suitable for log files.
     * @param file File to write to.
     */
    /*
    private static BufferedWriter getBufferedWriterForFile(File file) throws Exception {
        //AnsiConsole.systemInstall(); // To make sure that the console is running
        AnsiOutputStream out = new AnsiOutputStream(new FileOutputStream(file));
        return new BufferedWriter(new OutputStreamWriter(out));
    }
    */

    /**
     * ONLY FOR JANSI v2+ <br>
     * Returns a new {@link BufferedWriter} for the given file.
     * This writer is ANSI free, thus perfectly suitable for log files.
     */
    private static BufferedWriter getBufferedWriterForFile(File file) throws Exception {
        AnsiConsole.systemInstall(); // To make sure that the console is running
        AnsiPrintStream origOut = AnsiConsole.out();
        AnsiOutputStream out = new AnsiOutputStream(
                new FileOutputStream(file),
                origOut::getTerminalWidth,
                origOut.getMode(),
                null,
                origOut.getType(),
                origOut.getColors(),
                Charset.defaultCharset(),
                null,
                null,
                true
        );
        return new BufferedWriter(new OutputStreamWriter(out));
    }


    public static void close() {
        try {
            if (bw != null)
                bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void writeToLog(Ansi ansi) {
        writeToLog(ansi.toString());
    }

    public static synchronized void writeToLog(String string) {
        try {
            bw.write(string);
            // bw.newLine(); Please include the next line character in the string
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
