/*
 * Copyright Osiris Team
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * AutoPlug License.  Please consult the file "LICENSE" for details.
 */

package com.osiris.autoplug.core.logger;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiOutputStream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
public class LogFileWriter {
    public static File logFile;
    private static FileOutputStream fos;
    private static AnsiOutputStream out;
    private static BufferedWriter bw;

    public static void createLogWriter(File file){
        logFile = file;
        try{
            fos = new FileOutputStream(logFile);
            out = new AnsiOutputStream(fos);
            bw = new BufferedWriter(new OutputStreamWriter(out));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void close(){
        try{
            if (bw!=null)
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void writeToLog(Ansi ansi){
        writeToLog(ansi.toString());
    }

    public static synchronized void writeToLog(String string){
        try{
            bw.write(string);
            // bw.newLine(); Please include the next line character in the string
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
