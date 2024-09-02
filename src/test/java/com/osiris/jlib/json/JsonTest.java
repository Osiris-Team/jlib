package com.osiris.jlib.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.osiris.jlib.json.exceptions.HttpErrorException;
import com.osiris.jlib.json.exceptions.WrongJsonTypeException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JsonTest {

    @Test
    void testJsonFromUrl() throws IOException, HttpErrorException, WrongJsonTypeException {
        JsonElement el = Json.get("https://jsonplaceholder.typicode.com/todos/1");
        System.out.println(el);
        el = Json.getAsObject("https://jsonplaceholder.typicode.com/todos/1");
        System.out.println(el);
        el = Json.getAsJsonArray("https://jsonplaceholder.typicode.com/posts/1/comments");
        System.out.println(el);
        List<JsonObject> objs = Json.getAsList("https://jsonplaceholder.typicode.com/posts/1/comments");
        assertFalse(objs.isEmpty());
    }
}