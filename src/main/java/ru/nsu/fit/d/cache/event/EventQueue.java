package ru.nsu.fit.d.cache.event;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventQueue {
	
	private Queue<Event> queue;
	
	public EventQueue() {
		this.queue = new ConcurrentLinkedQueue<Event>();
	}
	
	// TODO: 18.01.20 методы по работе с очередью
}
