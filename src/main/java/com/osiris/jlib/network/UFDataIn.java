/*
 * Copyright Osiris Team
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * AutoPlug License.  Please consult the file "LICENSE" for details.
 */

package com.osiris.jlib.network;

import javax.naming.LimitExceededException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Ultra-fast DataInputStream, which additionally allows
 * transfer of files and streams.
 */
public class UFDataIn extends DataInputStream{


    public UFDataIn(InputStream inputStream) {
        super(inputStream);
    }

    /**
     * @param file write receiving data to this file.
     */
    public long readFile(File file) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            return readStream(out);
        }
    }

    /**
     * @param file write receiving data to this file.
     * @param maxBytes set to -1 if no limit wanted.
     */
    public long readFile(File file, long maxBytes) throws IOException, LimitExceededException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            return readStream(out, maxBytes);
        }
    }

    /**
     * @param out write receiving data to this stream.
     */
    public long readStream(OutputStream out) throws IOException {
        try{
            return readStream(out, -1);
        } catch (LimitExceededException e) { // Not excepted to happen since no limit
            throw new RuntimeException(e);
        }
    }

    /**
     * @param out write receiving data to this stream.
     * @param maxBytes set to -1 if no limit wanted.
     */
    public long readStream(OutputStream out, long maxBytes) throws IOException, LimitExceededException {
        /*
         * Handling Non-Text Data: If the input stream
         *  contains binary data (like images or other non-text files),
         *  Base64 encoding allows this data to be represented as text,
         *  which can then be safely written using writeUTF.
         * By using Base64 encoding, we ensure that the binary data is first converted
         *  to a string representation that can be safely written using writeUTF.
         */
        Base64.Decoder decoder = Base64.getDecoder();
        long countBytesRead = 0;
        int count;
        byte[] buffer = new byte[8192]; // or 4096, or more
        String buffer_s = "";
        while (!(buffer_s = readUTF()).equals(UFDataOut.EOF)) {
            buffer = decoder.decode(buffer_s.getBytes(StandardCharsets.UTF_8));
            count = buffer.length;

            countBytesRead += count;
            if (maxBytes >= 0 && countBytesRead > maxBytes) {
                throw new LimitExceededException("Exceeded the maximum allowed bytes: " + maxBytes);
            }
            out.write(buffer, 0, count);
            out.flush();
        }
        return countBytesRead;
        //read("\u001a") // Not needed here since already read above by read()
    }

}
