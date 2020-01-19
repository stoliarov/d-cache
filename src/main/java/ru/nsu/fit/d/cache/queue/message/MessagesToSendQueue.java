package ru.nsu.fit.d.cache.queue.message;

import ru.nsu.fit.d.cache.channel.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessagesToSendQueue<T> {
	
	private BlockingQueue<Message<T>> queue;
	
	public MessagesToSendQueue() {
		this.queue = new LinkedBlockingQueue<>();
	}
	
	public boolean offer(Message<T> message) {
		return queue.offer(message);
	}
	
	public Message<T> take() throws InterruptedException {
		return queue.take();
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
}
