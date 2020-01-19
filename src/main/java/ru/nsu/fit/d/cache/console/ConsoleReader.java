package ru.nsu.fit.d.cache.console;


import ru.nsu.fit.d.cache.queue.event.Event;
import ru.nsu.fit.d.cache.queue.event.EventType;

import java.util.Scanner;

public class ConsoleReader implements Runnable {
    private final int MAX_MESSAGE_SYMBOLS = 32000;

    private EventQueue eventQueue;

    public ConsoleReader(EventQueue eventQueue) {
        this.eventQueue = eventQueue;
    }

    public void run() {
        Scanner in = new Scanner(System.in);

        while (true) {
            System.out.println("Inter the key: ");
            String key = in.nextLine();
            key = trimStringIfNeeded(key);

            System.out.println("Inter the value: ");
            String value = in.nextLine();
            value = trimStringIfNeeded(value);

            Data consoleInput = new Data(key, value);
            Event event = new Event();
            event.setEventType(EventType.WRITE_TO_STORE);
            event.setKey(key);
            event.setValue(value);
            eventQueue.add(event);
        }
    }

    private String trimStringIfNeeded(String text) {
        if (text.length() > MAX_MESSAGE_SYMBOLS) {
            return text.substring(0, MAX_MESSAGE_SYMBOLS - 1);
        }

        return text;
    }
}
