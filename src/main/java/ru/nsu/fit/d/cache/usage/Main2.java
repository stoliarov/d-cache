package ru.nsu.fit.d.cache.usage;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import ru.nsu.fit.d.cache.channel.Response;
import ru.nsu.fit.d.cache.service.Node;

public class Main2 {
	
	public static void main(String[] args) {
		ZContext context = new ZContext();
		ZMQ.Socket client = context.createSocket(SocketType.DEALER);

		client.connect("tcp://*:10101");

		ZMsg message = new ZMsg();
		message.append("Hello");

		message.send(client);

//		Response response = getResponse(client, context);
//		System.out.println(response.getText());
		context.destroySocket(client);

//		Node node = new Node();
//		node.run();
	}
}
