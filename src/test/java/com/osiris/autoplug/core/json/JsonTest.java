package com.osiris.autoplug.core.json;

import com.osiris.autoplug.core.json.exceptions.HttpErrorException;
import com.osiris.autoplug.core.json.exceptions.WrongJsonTypeException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class JsonTest {

    @Test
    void testJsonFromUrl() throws IOException, HttpErrorException, WrongJsonTypeException {
        Json.get("https://jsonplaceholder.typicode.com/todos/1");
        Json.getAsObject("https://jsonplaceholder.typicode.com/todos/1");
        Json.getAsJsonArray("https://jsonplaceholder.typicode.com/posts/1/comments");
        Json.getAsList("https://jsonplaceholder.typicode.com/posts/1/comments");
    }
}