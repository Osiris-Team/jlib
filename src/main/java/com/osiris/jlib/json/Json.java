package com.osiris.jlib.json;

import com.google.gson.*;
import com.osiris.jlib.json.exceptions.HttpErrorException;
import com.osiris.jlib.json.exceptions.WrongJsonTypeException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
     * Returns the json-element. This can be a json-array or a json-object.
     *
     * @param url The url which leads to the json file.
     * @return JsonElement
     * @throws HttpErrorException When status code outside of range 200 to 299 (or not one of the provided success codes).
     */
    public static JsonElement get(String requestMethod, String url, JsonElement elementToSend, Integer... successCodes) throws IOException, HttpErrorException {
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.addRequestProperty("User-Agent", "jlib by Osiris");
            con.addRequestProperty("Content-Type", "application/json");
            con.setConnectTimeout(1000);
            con.setRequestMethod(requestMethod);
            con.setDoOutput(true);
            con.setDoInput(true);
            con.connect();

            if (elementToSend != null) {
                OutputStream out = con.getOutputStream();
                try (OutputStreamWriter outWrt = new OutputStreamWriter(out)) {
                    try (BufferedReader inr = new BufferedReader(new StringReader(new Gson().toJson(elementToSend)))) {
                        String l = null;
                        while ((l = inr.readLine()) != null) {
                            outWrt.write(l);
                        }
                    }
                }
            } // After POST finishes get RESPONSE:

            int code = con.getResponseCode();
            if ((code > 199 && code < 300) || (successCodes != null && Arrays.asList(successCodes).contains(code))) {
                InputStream in = con.getInputStream();
                if (in != null)
                    try (InputStreamReader inr = new InputStreamReader(in)) {
                        return JsonParser.parseReader(inr);
                    }
            } else {
                JsonElement response = null;
                InputStream in = con.getErrorStream();
                if (in != null)
                    try (InputStreamReader inr = new InputStreamReader(in)) {
                        response = JsonParser.parseReader(inr);
                    }
                throw new HttpErrorException(code, null, "\nurl: " + url + " \nmessage: " + con.getResponseMessage() + "\njson: \n" + new GsonBuilder().setPrettyPrinting().create().toJson(response));
            }
        } catch (IOException | HttpErrorException e) {
            if (con != null) con.disconnect();
            throw e;
        } finally {
            if (con != null) con.disconnect();
        }
        return null;
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
