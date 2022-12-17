package com.osiris.jlib.json;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.osiris.jlib.Reflect;
import com.osiris.jlib.Stream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class JsonFile<T> {
    public static Gson parser = new GsonBuilder().registerTypeAdapter(File.class, new FileTypeAdapter())
            .setPrettyPrinting().create();

    public final File file;
    private final AtomicBoolean save = new AtomicBoolean(false);
    public T data;

    /**
     * @param file     can NOT null! Path to your .json file. If not exists gets created.
     * @param dataType can NOT be null! The data objects' type/class.
     */
    public JsonFile(File file, Class<T> dataType) {
        Objects.requireNonNull(file);
        this.file = file;
        try {
            synchronized (file) {
                if (!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                    this.data = Reflect.newInstance(dataType);
                    save(); // Write defaults
                } else // Read existing
                    this.data = parser.fromJson(new BufferedReader(new FileReader(file)), dataType);
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
                                parser.toJson(data, sw);
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
        parser.toJson(data, sw);
        return sw.toString();
    }

    public JsonFile<T> writeTo(OutputStream out) throws IOException {
        StringWriter sw = new StringWriter(); // Passing the filewriter directly results in a blank file
        parser.toJson(data, sw);
        Stream.write(sw.toString(), out);
        return this;
    }

    public JsonFile<T> writeTo(File file) throws IOException {
        writeTo(new FileOutputStream(file));
        return this;
    }

    public JsonFile<T> writeTo(Path file) throws IOException {
        writeTo(new FileOutputStream(file.toFile()));
        return this;
    }
}
