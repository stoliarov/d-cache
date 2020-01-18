package ru.nsu.fit.d.cache.channel;

import com.sun.istack.internal.Nullable;
import lombok.AllArgsConstructor;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import java.nio.charset.Charset;

@AllArgsConstructor
public class Sender {
	
	private static final int TIMEOUT = 1000;
	
	private static final Charset CHARSET = Charset.defaultCharset();
	
	@Nullable
	public Response send(ZContext context, String url, Request request) {
		
		// TODO: 18.01.20 На само деле пока хз можно оставить отправку блокирующейся и возвращающей ответ,
		//   либо возвращающей управление сразу, но при этом складывающей полученные ответы в очередь EventQueue.
		//  Думаю, лучше второе
		
		Socket client = context.createSocket(SocketType.REQ);
		
		client.connect(url);
		
		ZMsg message = new ZMsg();
		message.append(request.getText());
		
		message.send(client);
		
		Response response = getResponse(client, context);
		
		context.destroySocket(client);
		
		return response;
	}
	
	private Response getResponse(Socket client, ZContext context) {
		
		Poller poller = context.createPoller(1);
		poller.register(client, Poller.POLLIN);
		
		poller.poll(TIMEOUT);
		
		if (!poller.pollin(0)) {
			return null;
		}
		
		ZMsg zMessageResponse = ZMsg.recvMsg(client);
		ZFrame first = zMessageResponse.getFirst();
		
		String receivedString = first.getString(CHARSET);
		
		Response response = new Response();
		
		response.setOk(true);
		response.setText(receivedString);
		
		return response;
	}
}
