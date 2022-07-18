package com.osiris.autoplug.core.json;

import com.osiris.autoplug.core.json.exceptions.HttpErrorException;
import com.osiris.autoplug.core.json.exceptions.WrongJsonTypeException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class JsonTest {

    @Test
    void testJsonFromUrl() throws IOException, HttpErrorException, WrongJsonTypeException {
        Json tools = new Json();
        tools.fromUrl("https://jsonplaceholder.typicode.com/todos/1");
        tools.fromUrlAsObject("https://jsonplaceholder.typicode.com/todos/1");
        tools.fromUrlAsJsonArray("https://jsonplaceholder.typicode.com/posts/1/comments");
        tools.fromUrlAsList("https://jsonplaceholder.typicode.com/posts/1/comments");
    }
}