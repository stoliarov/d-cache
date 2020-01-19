package ru.nsu.fit.d.cache.queue.event;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventQueue<T> {
	
	private BlockingQueue<Event<T>> queue;
	
	public EventQueue() {
		this.queue = new LinkedBlockingQueue<>();
	}
	
	public boolean offer(Event<T> event) {
		return queue.offer(event);
	}
	
	public Event<T> take() throws InterruptedException {
		return queue.take();
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
}
