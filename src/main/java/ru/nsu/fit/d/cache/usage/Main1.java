package ru.nsu.fit.d.cache.usage;

import org.zeromq.*;
import ru.nsu.fit.d.cache.channel.Response;
import ru.nsu.fit.d.cache.console.Data;
import ru.nsu.fit.d.cache.event.Event;
import ru.nsu.fit.d.cache.messages.Message;
import ru.nsu.fit.d.cache.messages.MessageType;
import ru.nsu.fit.d.cache.messages.Serializer;

import java.nio.charset.Charset;

public class Main1 {
	
	public static void main(String[] args) {
		ZContext context = new ZContext();
		ZMQ.Socket client = context.createSocket(SocketType.ROUTER);

		client.connect("tcp://*:10101");

//		ZMsg message = new ZMsg();
//		message.append("Hello");
//
//		message.send(client);

		Response response = getResponse(client, context);
		System.out.println(response.getText());
		context.destroySocket(client);

//		Message message = new Message(MessageType.PUT, new Data("somekey", "somevalue"));
//		String jsonString = null;
//
//		var event = new Event(null);
//
//		jsonString = Serializer.getJsonString(message);
//
//
//		var m = Serializer.deserializeMessage(jsonString);

//		Node node = new Node();
//		node.run();
	}
	private static Response getResponse(ZMQ.Socket client, ZContext context) {

		ZMQ.Poller poller = context.createPoller(1);
		poller.register(client, ZMQ.Poller.POLLIN);

		poller.poll(1000);

		if (!poller.pollin(0)) {
			return null;
		}

		ZMsg zMessageResponse = ZMsg.recvMsg(client);
		ZFrame first = zMessageResponse.getFirst();

		String receivedString = first.getString(Charset.defaultCharset());

		Response response = new Response();

		response.setOk(true);
		response.setText(receivedString);

		return response;
	}
}
