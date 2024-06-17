package com.osiris.jlib.json;

import com.google.gson.*;
import com.osiris.jlib.Stream;
import com.osiris.jlib.json.exceptions.HttpErrorException;
import com.osiris.jlib.json.exceptions.WrongJsonTypeException;
import okhttp3.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Json {

    /**
     * Reads/Parses the provided String to a {@link JsonElement}.
     */
    public static JsonElement fromString(String s) {
        return JsonParser.parseString(s);
    }

    /**
     * Writes/Parses the provided {@link JsonElement} to a String.
     */
    public static String toString(JsonElement el) {
        return new Gson().toJson(el);
    }

    public static JsonElement get(String url) throws IOException, HttpErrorException {
        return get("GET", url, null, (Integer[]) null);
    }

    /**
     * Expects a {@link JsonArray}, otherwise throws {@link WrongJsonTypeException}. <br>
     * See {@link #(String, String, JsonElement, Integer...)} for details.
     */
    public static JsonArray getAsJsonArray(String url) throws IOException, HttpErrorException, WrongJsonTypeException {
        JsonElement element = get(url);
        if (element != null && element.isJsonArray()) {
            return element.getAsJsonArray();
        } else {
            throw new WrongJsonTypeException("Its not a json array! Check it out -> " + url);
        }
    }

    /**
     * Expects a {@link JsonArray} containing {@link JsonObject}s, that gets converted to a {@link List}, otherwise throws {@link WrongJsonTypeException}. <br>
     * See {@link #get(String, String, JsonElement, Integer...)} for details.
     */
    public static List<JsonObject> getAsList(String url) throws IOException, HttpErrorException, WrongJsonTypeException {
        List<JsonObject> objectList = new ArrayList<>();
        JsonElement element = get(url);
        if (element != null && element.isJsonArray()) {
            final JsonArray ja = element.getAsJsonArray();
            for (int i = 0; i < ja.size(); i++) {
                JsonObject jo = ja.get(i).getAsJsonObject();
                objectList.add(jo);
            }
            return objectList;
        } else {
            throw new WrongJsonTypeException("Its not a json array! Check it out -> " + url);
        }
    }

    /**
     * Expects a {@link JsonObject}, otherwise throws {@link WrongJsonTypeException}. <br>
     * See {@link #get(String, String, JsonElement, Integer...)} for details.
     */
    public static JsonObject getAsObject(String url) throws IOException, HttpErrorException, WrongJsonTypeException {
        JsonElement element = get(url);
        if (element != null && element.isJsonObject()) {
            return element.getAsJsonObject();
        } else {
            throw new WrongJsonTypeException("Its not a json object! Check it out -> " + url);
        }
    }

    public static JsonElement get(String requestMethod, String url) throws IOException, HttpErrorException {
        return get(requestMethod, url, null, (Integer[]) null);
    }

    public static JsonElement get(String requestMethod, String url, JsonElement elementToSend) throws IOException, HttpErrorException {
        return get(requestMethod, url, elementToSend, (Integer[]) null);
    }

    /**
     * Performs an HTTP request using OkHttp.
     * Returns the json-element. This can be a json-array or a json-object.
     *
     * @param requestMethod  The HTTP request method ("GET" or "POST").
     * @param url            The URL to fetch data from.
     * @param elementToSend  The JSON element to send (null for GET requests).
     * @param successCodes   Success HTTP status codes.
     * @return JsonElement containing the parsed JSON response.
     * @throws IOException         If an error occurs during the request.
     * @throws HttpErrorException If the HTTP response status code is outside the success range or not one of the provided success codes.
     */
    public static JsonElement get(String requestMethod, String url, JsonElement elementToSend, Integer... successCodes) throws IOException, HttpErrorException {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS) // Adjust timeouts as needed
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "jlib by Osiris")
                .addHeader("Content-Type", "application/json");

        if (elementToSend != null) {
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(elementToSend));
            requestBuilder.method(requestMethod, requestBody);
        } else {
            requestBuilder.method(requestMethod, null);
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            int code = response.code();

            if (!isSuccess(code, successCodes)) {
                throw new HttpErrorException(code, null, "\nurl: " + url + " \nmessage: " + response.message() + "\njson: \n" + responseBody.string());
            }

            String responseJson = responseBody.string();
            return JsonParser.parseString(responseJson);
        }
    }

    /**
     * Checks if the HTTP response code is in the success range or matches any of the provided success codes.
     *
     * @param code         The HTTP response code to check.
     * @param successCodes Success HTTP status codes.
     * @return true if the code is in the success range or matches any of the success codes, false otherwise.
     */
    private static boolean isSuccess(int code, Integer... successCodes) {
        if (code >= 200 && code < 300) {
            return true;
        }
        if (successCodes != null) {
            return Arrays.asList(successCodes).contains(code);
        }
        return false;
    }

    public static JsonElement post(String url, JsonElement element) throws IOException, HttpErrorException {
        return get("POST", url, element, (Integer[]) null);
    }

    public static JsonElement post(String url, JsonElement element, Integer... successCodes) throws IOException, HttpErrorException {
        return get("POST", url, element, successCodes);
    }

    public static JsonElement patch(String url, JsonElement element) throws IOException, HttpErrorException {
        return get("PATCH", url, element, (Integer[]) null);
    }

    public static JsonElement patch(String url, JsonElement element, Integer... successCodes) throws IOException, HttpErrorException {
        return get("PATCH", url, element, successCodes);
    }

    public static JsonElement delete(String url) throws IOException, HttpErrorException {
        return get("DELETE", url, null, 204);
    }

    public static JsonElement delete(String url, Integer... successCodes) throws IOException, HttpErrorException {
        return get("DELETE", url, null, successCodes);
    }
}
