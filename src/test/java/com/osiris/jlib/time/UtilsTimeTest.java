package com.osiris.jlib.time;

import org.junit.jupiter.api.Test;

class UtilsTimeTest {

    @Test
    void getFormattedString() {
        long ms = 60012387000L;
        System.out.println(new UtilsTime().getFormattedString(ms));
    }
}