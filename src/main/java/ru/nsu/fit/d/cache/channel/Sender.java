package ru.nsu.fit.d.cache.channel;

import lombok.AllArgsConstructor;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;
import ru.nsu.fit.d.cache.queue.message.MessagesToSendQueue;

@AllArgsConstructor
public class Sender<T> implements Runnable {
	
	private MessagesToSendQueue<T> messagesToSendQueue;
	
	@Override
	public void run() {
		
		while(true) {
			try {
				send(messagesToSendQueue.take());
				
			} catch (InterruptedException e) {
				// TODO: 19.01.20 close socket
				return;
			}
		}
	}
	
	private void send(Message<T> message) {
		// TODO: 19.01.20
	}
	
	private void send(String url, Message request) {
		
		ZContext context = new ZContext();
		
		Socket client = context.createSocket(SocketType.REQ);
		
		client.connect(url);
		
		ZMsg message = new ZMsg();
		message.append("some string");
		
		message.send(client);
		
		context.destroySocket(client);
	}
}
