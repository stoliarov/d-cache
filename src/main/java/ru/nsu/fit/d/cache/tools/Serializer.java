package ru.nsu.fit.d.cache.tools;

import com.google.gson.Gson;
import ru.nsu.fit.d.cache.channel.Message;

import java.io.Serializable;

public class Serializer {

    public static String getJsonString(Object obj) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(obj);

        return jsonString;
    }

    public static Message deserializeMessage(String jsonString) {
        Gson gson = new Gson();
        Message message = gson.fromJson(jsonString, Message.class);

        return message;
    }
}