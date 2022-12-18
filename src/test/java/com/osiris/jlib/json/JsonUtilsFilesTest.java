package com.osiris.jlib.json;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonUtilsFilesTest {

    @Test
    void save() throws IOException, IllegalAccessException, InvocationTargetException, InstantiationException {

        // Check defaults
        Person person = new Person(new File(System.getProperty("user.dir") + "/person.json"));
        assertEquals("Peter", person.name);
        assertEquals(44, person.age);
        assertEquals(new File(System.getProperty("user.dir") + "/data.txt").toString(), person.file.toString());

        // Test loading from file
        person.name = "Franz";
        person.age = 55;
        person.saveNow();
        person.load();
        assertEquals("Franz", person.name);
        assertEquals(55, person.age);

        // Test loading from file in new object
        Person person2 = new Person(new File(System.getProperty("user.dir") + "/person.json"));
        person2.load();
        assertEquals("Franz", person2.name);
        assertEquals(55, person2.age);

        // Test loading defaults
        person.loadDefaults();
        assertEquals("Peter", person.name);
        assertEquals(44, person.age);

        // Test delete
        person.getJsonFile().delete();
    }
}