package com.osiris.jlib.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Output {
    public Channel socket;

    public Output(Channel socket) {
        this.socket = socket;
    }

    /**
     * Write a/multiple bytes, aka raw data.
     */
    public synchronized void writeBytes(byte[] b) {
        socket.write(b);
        flush();
    }

    /**
     * Write a/multiple bytes, aka raw data.
     */
    public synchronized void writeBytes(ByteBuf b) {
        socket.write(b);
        flush();
    }

    /**
     * Submit/Send the buffered data.
     */
    public void flush() {
        socket.flush();
    }

    /**
     * Write a string.
     */
    public void writeUTF(String s){
        socket.write(s);
        flush();
    }


    /**
     * Writes a {@code boolean} to the underlying output stream as
     * a 1-byte value. The value {@code true} is written out as the
     * value {@code (byte)1}; the value {@code false} is
     * written out as the value {@code (byte)0}. If no exception is
     * thrown, the counter {@code written} is incremented by
     * {@code 1}.
     *
     * @param      v   a {@code boolean} value to be written.
     * @throws     IOException  if an I/O error occurs.
     */
    public final void writeBoolean(boolean v) throws IOException {
        socket.write(v);
        flush();
    }

    /**
     * Writes a {@code short} to the underlying output stream as two
     * bytes, high byte first. If no exception is thrown, the counter
     * {@code written} is incremented by {@code 2}.
     *
     * @param      v   a {@code short} to be written.
     * @throws     IOException  if an I/O error occurs.
     * 
     */
    public final void writeShort(short v) throws IOException {
        socket.write(v);
        flush();
    }

    /**
     * Writes an {@code int} to the underlying output stream as four
     * bytes, high byte first. If no exception is thrown, the counter
     * {@code written} is incremented by {@code 4}.
     *
     * @param      v   an {@code int} to be written.
     * @throws     IOException  if an I/O error occurs.
     * 
     */
    public final void writeInt(int v) throws IOException {
        socket.write(v);
        flush();
    }

    /**
     * Writes a {@code long} to the underlying output stream as eight
     * bytes, high byte first. In no exception is thrown, the counter
     * {@code written} is incremented by {@code 8}.
     *
     * @param      v   a {@code long} to be written.
     * @throws     IOException  if an I/O error occurs.
     * 
     */
    public final void writeLong(long v) throws IOException {
        socket.write(v);
        flush();
    }

    /**
     * Converts the float argument to an {@code int} using the
     * {@code floatToIntBits} method in class {@code Float},
     * and then writes that {@code int} value to the underlying
     * output stream as a 4-byte quantity, high byte first. If no
     * exception is thrown, the counter {@code written} is
     * incremented by {@code 4}.
     *
     * @param      v   a {@code float} value to be written.
     * @throws     IOException  if an I/O error occurs.
     * 
     * @see        java.lang.Float#floatToIntBits(float)
     */
    public final void writeFloat(float v) throws IOException {
        socket.write(v);
        flush();
    }

    /**
     * Converts the double argument to a {@code long} using the
     * {@code doubleToLongBits} method in class {@code Double},
     * and then writes that {@code long} value to the underlying
     * output stream as an 8-byte quantity, high byte first. If no
     * exception is thrown, the counter {@code written} is
     * incremented by {@code 8}.
     *
     * @param      v   a {@code double} value to be written.
     * @throws     IOException  if an I/O error occurs.
     * 
     * @see        java.lang.Double#doubleToLongBits(double)
     */
    public final void writeDouble(double v) throws IOException {
        socket.write(v);
        flush();
    }
}
