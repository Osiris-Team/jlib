package com.osiris.jlib;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class StreamTest {

    @Test
    void testToString() throws Exception{
        ByteArrayInputStream in = new ByteArrayInputStream("Hello World!".getBytes(StandardCharsets.UTF_8));
        assertEquals("Hello World!", Stream.toString(in));
    }
}