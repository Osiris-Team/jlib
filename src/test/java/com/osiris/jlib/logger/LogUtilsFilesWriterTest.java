/*
 * Copyright Osiris Team
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * AutoPlug License.  Please consult the file "LICENSE" for details.
 */

package com.osiris.jlib.logger;

import org.fusesource.jansi.Ansi;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LogUtilsFilesWriterTest {

    @Test
    void writeToLogTest() {
        File file = new File(System.getProperty("user.dir") + "/src/test/test.log");
        try {
            if (file.exists())
                file.delete();
            file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogFileWriter.setLogWriterForFile(file);
        LogFileWriter.writeToLog(Ansi.ansi().fg(Ansi.Color.BLUE).a("Hello!"));
        assertTrue(file.length() > 0);
    }

}