package com.osiris.autoplug.core.json;

import com.osiris.autoplug.core.json.exceptions.HttpErrorException;
import com.osiris.autoplug.core.json.exceptions.WrongJsonTypeException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class JsonToolsTest {

    @Test
    void testJsonFromUrl() throws IOException, HttpErrorException, WrongJsonTypeException {
        JsonTools tools = new JsonTools();
        tools.getJsonElement("https://jsonplaceholder.typicode.com/todos/1");
        tools.getJsonObject("https://jsonplaceholder.typicode.com/todos/1");
        tools.getJsonArray("https://jsonplaceholder.typicode.com/posts/1/comments");
        tools.getJsonArrayAsList("https://jsonplaceholder.typicode.com/posts/1/comments");
    }
}