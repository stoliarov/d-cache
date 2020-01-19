package ru.nsu.fit.d.cache.event;

import org.apache.commons.lang3.Validate;
import ru.nsu.fit.d.cache.console.Data;

public class Event {
    private EventType type;
    private Data consoleInput;

    public Event(EventType type) {
        this.type = type;
    }

    public void setConsoleInput(Data consoleInput) {
        Validate.notNull(consoleInput, "console input cannot be null");

        this.consoleInput = consoleInput;
    }
}
