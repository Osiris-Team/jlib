package com.osiris.autoplug.core.search;

import java.util.Objects;

public class Version {

    /**
     * Compares the current version with the latest
     * version and returns true if the latest version is
     * bigger than the current version. <br>
     * Note that empty strings are handled as 0. <br>
     * If the version is malformed like 1..0 or 1.0...1 the additional dots get removed.
     */
    public static boolean isLatestBigger(String currentVersion, String latestVersion) {
        String[] arrCurrent = cleanAndSplitByDots(currentVersion);
        String[] arrLatest = cleanAndSplitByDots(latestVersion);

        if (arrLatest.length == arrCurrent.length) {
            int latest, current;
            for (int i = 0; i < arrLatest.length; i++) {
                latest = Integer.parseInt(arrLatest[i]);
                current = Integer.parseInt(arrCurrent[i]);
                if (latest == current) continue;
                else return latest > current;
            }
            return false; // All are the same
        } else
            return arrLatest.length > arrCurrent.length;

    }

    /**
     * Compares the current version with the latest
     * version and returns true if the latest version is
     * bigger or equal to the current version. <br>
     * Note that empty strings are handled as 0. <br>
     * If the version is malformed like 1..0 or 1.0...1 the additional dots get removed.
     */
    public static boolean isLatestBiggerOrEqual(String currentVersion, String latestVersion) {
        String[] arrCurrent = cleanAndSplitByDots(currentVersion);
        String[] arrLatest = cleanAndSplitByDots(latestVersion);

        if (arrLatest.length == arrCurrent.length) {
            int latest, current;
            for (int i = 0; i < arrLatest.length; i++) {
                latest = Integer.parseInt(arrLatest[i]);
                current = Integer.parseInt(arrCurrent[i]);
                if (latest == current) continue;
                else return latest >= current;
            }
            return true; // All are the same
        } else return arrLatest.length >= arrCurrent.length;
    }

    private static String[] cleanAndSplitByDots(String version) {
        Objects.requireNonNull(version);
        version = version.trim() // Remove left and right spaces
                .replaceAll("[^0-9.]", ""); // Remove everything except numbers and dots
        while (version.contains("..")){
            // 0.0..1 -> 0.0.1, or 0...1 -> 0.1, works with as many dots as you want
            version = version.replaceAll("\\.\\.", ".");
        }
        if (version.isEmpty()) return new String[]{"0"};
        return version.split("\\."); // Split string by .
    }

}
