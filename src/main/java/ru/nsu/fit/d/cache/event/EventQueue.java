package ru.nsu.fit.d.cache.event;

import org.apache.commons.lang3.Validate;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventQueue {

    private Queue<Event> queue;

    public EventQueue() {
        this.queue = new ConcurrentLinkedQueue<Event>();
    }

    public void add(Event event) {
        Validate.notNull(event, "event cannot be null");

        queue.add(event);
    }
}
