package com.osiris.jlib.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class VersionTest {
    @Test
    void test() {
        String current = "1.0.0";
        String latest = "1.0.1";
        assertTrue(Version.isLatestBigger(current, latest));
        assertTrue(Version.isLatestBiggerOrEqual(current, latest));
        latest = "1.0.0";
        assertTrue(Version.isLatestBiggerOrEqual(current, latest));
    }
}
