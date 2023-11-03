package com.osiris.jlib.search;

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
            String latest, current;
            for (int i = 0; i < arrLatest.length; i++) {
                latest = arrLatest[i];
                current = arrCurrent[i];
                if (latest.equals(current)) continue;
                else return isFirstBigger(latest, current);
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
            String latest, current;
            for (int i = 0; i < arrLatest.length; i++) {
                latest = arrLatest[i];
                current = arrCurrent[i];
                if (latest.equals(current)) continue;
                else return isFirstBiggerOrEqual(latest, current);
            }
            return true; // All are the same
        } else return arrLatest.length >= arrCurrent.length;
    }

    public static String[] cleanAndSplitByDots(String version) {
        Objects.requireNonNull(version);
        version = version.trim() // Remove left and right spaces
                .replaceAll("[^0-9.]", ""); // Remove everything except numbers and dots
        while (version.contains("..")) {
            // 0.0..1 -> 0.0.1, or 0...1 -> 0.1, works with as many dots as you want
            version = version.replaceAll("\\.\\.", ".");
        }
        if (version.isEmpty()) return new String[]{"0"};
        return version.split("\\."); // Split string by .
    }

    public static boolean isFirstBigger(String num1, String num2) {
        if (num1.length() > num2.length()) {
            return true;
        } else if (num1.length() < num2.length()) {
            return false;
        } else {
            // If the lengths are equal, compare character by character.
            for (int i = 0; i < num1.length(); i++) {
                char digit1 = num1.charAt(i);
                char digit2 = num2.charAt(i);
                if (digit1 > digit2) {
                    return true;
                } else if (digit1 < digit2) {
                    return false;
                }
            }
        }
        // If all characters are the same, consider them equal.
        return false;
    }

    public static boolean isFirstBiggerOrEqual(String num1, String num2) {
        if (num1.length() >= num2.length()) {
            return true;
        } else if (num1.length() < num2.length()) {
            return false;
        } else {
            // If the lengths are equal, compare character by character.
            for (int i = 0; i < num1.length(); i++) {
                char digit1 = num1.charAt(i);
                char digit2 = num2.charAt(i);
                if (digit1 > digit2) {
                    return true;
                } else if (digit1 < digit2) {
                    return false;
                }
            }
        }
        // If all characters are the same, consider them equal.
        return true;
    }

}
