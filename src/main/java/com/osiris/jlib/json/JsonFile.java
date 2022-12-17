package com.osiris.jlib.json;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.osiris.jlib.Reflect;
import com.osiris.jlib.Stream;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class JsonFile {
    public static transient Gson parser = new GsonBuilder().registerTypeAdapter(File.class, new FileTypeAdapter())
            .excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT, java.lang.reflect.Modifier.PRIVATE) // ONLY exclude transient, to allow including static fields too
            .setPrettyPrinting().create();

    private final AtomicBoolean save = new AtomicBoolean(false);
    private final File file;

    /**
     * @param file     can NOT null! Path to your .json file. If not exists gets created.
     * @param dataType can NOT be null! The data objects' type/class.
     */
    public JsonFile(File file) {
        Objects.requireNonNull(file);
        this.file = file;
        try {
            synchronized (file) {
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                    save(); // Write defaults
                } else { // Read existing
                    Class<?> clazz = getClass();
                    Object instance = parser.fromJson(new BufferedReader(new FileReader(file)), getClass());
                    for (Field field : clazz.getDeclaredFields()) {
                        field.setAccessible(true);
                        field.set(this, field.get(instance));
                    }
                }

            }
            new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep(1000);
                        if (save.get()) {
                            synchronized (file) {
                                if (!file.exists()) {
                                    file.getParentFile().mkdirs();
                                    file.createNewFile();
                                }
                                StringWriter sw = new StringWriter(); // Passing the filewriter directly results in a blank file
                                parser.toJson(this, sw);
                                String out = sw.toString();
                                Files.write(file.toPath(), out.getBytes(StandardCharsets.UTF_8));
                            }
                            save.set(false);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public File getJsonFile(){
        return file;
    }

    public void save() {
        save.set(true);
    }

    public void saveNow() {
        save();
        try {
            while (save.get()) Thread.sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String toPrintString() {
        StringWriter sw = new StringWriter(); // Passing the filewriter directly results in a blank file
        parser.toJson(this, sw);
        return sw.toString();
    }

    public void writeTo(OutputStream out) throws IOException {
        StringWriter sw = new StringWriter(); // Passing the filewriter directly results in a blank file
        parser.toJson(this, sw);
        Stream.write(sw.toString(), out);
    }

    public void writeTo(File file) throws IOException {
        writeTo(new FileOutputStream(file));
    }

    public void writeTo(Path file) throws IOException {
        writeTo(new FileOutputStream(file.toFile()));
    }
}
