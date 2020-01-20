package ru.nsu.fit.d.cache.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import ru.nsu.fit.d.cache.channel.Message;

import java.io.Serializable;
import java.io.StringReader;

public class Serializer {
    static Gson gson = new Gson();

    public static String getJsonString(Object obj) {
        return gson.toJson(obj);
    }

    public static Message deserializeMessage(String jsonString) {
        JsonReader jsonReader = new JsonReader(new StringReader(jsonString));
        jsonReader.setLenient(true);
        return gson.fromJson(jsonReader, Message.class);
    }
}