/*
 * Copyright (c) 2022 Osiris-Team.
 * All rights reserved.
 *
 * This software is copyrighted work, licensed under the terms
 * of the MIT-License. Consult the "LICENSE" file for details.
 */

package com.osiris.jlib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Stream {
    public static String toString(InputStream in) throws IOException {
        if (in == null) return null;
        StringBuilder s = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String l = null;
            while ((l = reader.readLine()) != null) {
                s.append(l).append("\n");
            }
        }
        return s.toString();
    }
    public static List<String> toList(InputStream in) throws IOException {
        List<String> list = new ArrayList<>();
        if (in == null) return list;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String l = null;
            while ((l = reader.readLine()) != null) {
                list.add(l);
            }
        }
        return list;
    }
}
