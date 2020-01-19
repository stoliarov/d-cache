package ru.nsu.fit.d.cache.messages;

import org.apache.commons.lang3.Validate;
import ru.nsu.fit.d.cache.console.Data;

public class Message {
    private MessageType type;
    private Data data;

    public Message(MessageType type, Data data) {
        Validate.notNull(type, "type cannot be null");
        Validate.notNull(data, "data cannot be null");

        this.type = type;
        this.data = data;
    }
}
