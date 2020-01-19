package ru.nsu.fit.d.cache.messages;

import com.google.gson.Gson;

public class Serializer {

    public static String getJsonString(Message message) {
        var gson = new Gson();
        var jsonString = gson.toJson(message);

        return jsonString;
    }

    public static Message deserializeMessage(String jsonString) {
        var gson = new Gson();
        var message = gson.fromJson(jsonString, Message.class);

        return message;
    }
}