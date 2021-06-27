/*
 * Copyright Osiris Team
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * AutoPlug License.  Please consult the file "LICENSE" for details.
 */

package com.osiris.autoplug.core.logger;

import org.fusesource.jansi.*;
import org.fusesource.jansi.io.AnsiOutputStream;

import java.io.*;
import java.nio.charset.Charset;

public class LogFileWriter {
    public static File LOG_FILE;
    public static BufferedWriter BUFFERED_WRITER;

    public static void setLogWriterForFile(File file) {
        LOG_FILE = file;
        try {
            BUFFERED_WRITER = getBufferedWriterForFile(LOG_FILE);
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
    public static BufferedWriter getBufferedWriterForFile(File file) throws Exception {
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
    public static BufferedWriter getBufferedWriterForFile(File file) throws Exception {
        return getBufferedWriterForOutputStream(new FileOutputStream(file));
    }

    /**
     * ONLY FOR JANSI v2+ <br>
     * Returns a new {@link BufferedWriter} for the given file.
     * This writer is ANSI free, thus perfectly suitable for log files.
     */
    public static BufferedWriter getBufferedWriterForOutputStream(OutputStream os) {
        AnsiOutputStream out = new AnsiOutputStream(os,
                () -> Integer.MAX_VALUE,
                AnsiMode.Strip,
                null,
                AnsiType.Native,
                AnsiColors.Colors16,
                Charset.defaultCharset(),
                null,
                null,
                true);
        return new BufferedWriter(new OutputStreamWriter(out));
    }


    public static void close() {
        try {
            if (BUFFERED_WRITER != null)
                BUFFERED_WRITER.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void writeToLog(Ansi ansi) {
        writeToLog(ansi.toString());
    }

    public static synchronized void writeToLog(String string) {
        try {
            BUFFERED_WRITER.write(string);
            // bw.newLine(); Please include the next line character in the string
            BUFFERED_WRITER.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
