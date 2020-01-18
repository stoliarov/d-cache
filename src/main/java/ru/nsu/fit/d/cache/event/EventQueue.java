package ru.nsu.fit.d.cache.event;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class EventQueue<T> {
	
	private BlockingQueue<Event<T>> queue;
	
	public EventQueue() {
		this.queue = new LinkedBlockingQueue<Event<T>>();
	}
	
	public boolean add(Event<T> event) {
		return queue.add(event);
	}
	
	public Event<T> poll(long timeout, TimeUnit timeUnit) throws InterruptedException {
		return queue.poll(timeout, timeUnit);
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
}
