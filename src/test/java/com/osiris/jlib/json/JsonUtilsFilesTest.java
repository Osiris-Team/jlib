package com.osiris.jlib.json;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonUtilsFilesTest {

    @Test
    void save() {
        JsonFile<Person> json = new JsonFile<>(new File(System.getProperty("user.dir") + "/person.json"),
                Person.class);
        assertEquals("Peter", json.data.name);
        assertEquals(44, json.data.age);
        assertEquals(new File(System.getProperty("user.dir") + "/data.txt").toString(), json.data.file.toString());
        json.saveNow();
        System.out.println(json.toPrintString());
        json.file.delete();
    }

    class Person {
        String name = "Peter";
        int age = 44;
        File file = new File(System.getProperty("user.dir") + "/data.txt");
    }
}