package ru.nsu.fit.d.cache.channel;

import lombok.AllArgsConstructor;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

@AllArgsConstructor
public class Sender {
	
	public void send(String url, Message request) {
		
		ZContext context = new ZContext();
		
		Socket client = context.createSocket(SocketType.REQ);
		
		client.connect(url);
		
		ZMsg message = new ZMsg();
		message.append("some string");
		
		message.send(client);
		
		context.destroySocket(client);
	}
}
