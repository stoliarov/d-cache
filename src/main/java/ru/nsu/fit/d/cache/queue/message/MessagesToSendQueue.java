package ru.nsu.fit.d.cache.queue.message;

import ru.nsu.fit.d.cache.channel.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessagesToSendQueue {
	
	private BlockingQueue<Message> queue;
	
	public MessagesToSendQueue() {
		this.queue = new LinkedBlockingQueue<>();
	}
	
	public boolean offer(Message message) {
		return queue.offer(message);
	}
	
	public Message take() throws InterruptedException {
		return queue.take();
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
}
