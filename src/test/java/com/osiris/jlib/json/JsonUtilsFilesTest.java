package com.osiris.jlib.json;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonUtilsFilesTest {

    @Test
    void save() throws IOException {
        Person json = new Person(new File(System.getProperty("user.dir") + "/person.json"));
        assertEquals("Peter", json.name);
        assertEquals(44, json.age);
        assertEquals(new File(System.getProperty("user.dir") + "/data.txt").toString(), json.file.toString());
        json.saveNow();
        System.out.println(json.toPrintString());
        json.getJsonFile().delete();
    }
}