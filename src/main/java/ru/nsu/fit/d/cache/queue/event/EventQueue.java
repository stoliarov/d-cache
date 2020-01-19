package ru.nsu.fit.d.cache.queue.event;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventQueue {
	
	private BlockingQueue<Event> queue;
	
	public EventQueue() {
		this.queue = new LinkedBlockingQueue<>();
	}
	
	public boolean offer(Event event) {
		return queue.offer(event);
	}
	
	public Event take() throws InterruptedException {
		return queue.take();
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	public void add(Event event) {
		queue.add(event);
	}
}
