package com.osiris.jlib.json;

import java.io.File;

public class Person extends JsonFile{
    public static String test = "tessss";
    String name = "Peter";
    int age = 44;
    File file = new File(System.getProperty("user.dir") + "/data.txt");

    /**
     * @param file can NOT null! Path to your .json file. If not exists gets created.
     */
    public Person(File file) {
        super(file);
    }
}
