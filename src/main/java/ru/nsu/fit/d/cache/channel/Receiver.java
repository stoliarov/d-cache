package ru.nsu.fit.d.cache.channel;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import ru.nsu.fit.d.cache.event.Event;
import ru.nsu.fit.d.cache.event.EventQueue;
import ru.nsu.fit.d.cache.event.EventType;

import java.io.Serializable;
import java.util.Arrays;

@AllArgsConstructor
public class Receiver<T extends Serializable> implements Runnable {
	private final Gson gson = new Gson();
	private static final int WAIT_TIMEOUT = 200;
	private String endpoint;
	private EventQueue<T> eventQueue;
	private int port;
	private final ZContext context = new ZContext();
	private final ZMQ.Socket multicastSocket = context.createSocket(SocketType.SUB);
	private final ZMQ.Socket forNewbieSocket = context.createSocket(SocketType.PAIR);

	public Receiver(int port, String endpoint, EventQueue<T> eventQueue) {
		this.port = port;
		this.endpoint = endpoint;
		this.eventQueue = eventQueue;
		multicastSocket.connect(endpoint + ":" + port);
	}

	public Message<T> receive() {
		// TODO: 18.01.20 Запускается в отдельном потоке, блокируется и ждет запросы.
		//  Кладет полученное в очередь как event. Далее из этой очереди класс Node будет выгребать запросы и реагировать
		multicastSocket.setReceiveTimeOut(WAIT_TIMEOUT);
		//reading
		byte[] buf = new byte[4096];
		StringBuilder messageBuilder = new StringBuilder();
		while(multicastSocket.recv(buf, 0, buf.length, 0) > 0) {
			messageBuilder.append(Arrays.toString(buf));
		}
		String messageString = messageBuilder.toString();
		Message<T> message = gson.fromJson(messageString, Message<T>.class);
		//TODO: 19.01.20 решить, что дедать с параметризованым классом Message
		return message;
	}

	public Message<T> receiveFromNewbie() {
		forNewbieSocket.setReceiveTimeOut(WAIT_TIMEOUT);
		//reading
		byte[] buf = new byte[4096];
		StringBuilder messageBuilder = new StringBuilder();
		while(multicastSocket.recv(buf, 0, buf.length, 0) > 0) {
			messageBuilder.append(Arrays.toString(buf));
		}
		String messageString = messageBuilder.toString();
		Message<T> message = (Message<T>)gson.fromJson(messageString, Message.class);
		//TODO: 19.01.20 решить, что дедать с параметризованым классом Message
		return message;
	}

	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			Message<T> message = receive();
			//TODO: 19.01.20 положить новый эвент согласно полученому сообщению
			Event<T> event = createEventByMessage(message);
			if(event != null)
				eventQueue.add(event);

			//получение сообщения от новичка
			Message<T> newbieMessage = receive();
			//TODO: 19.01.20 положить новый эвент согласно полученому сообщению
			Event<T> newbieEvent = createEventByMessage(message);
			if(event != null)
				eventQueue.add(event);
		}
	}

	private Event<T> createEventByMessage(Message<T> message) {
		if(message == null)
			return null;
		Event<T> event = new Event<T>();
		event.setValue(message.getValue());
		event.setResponseContext(message);
		event.setChangeId(message.getChangeId());
		event.setFromUrl(endpoint);
		event.setKey(message.getKey());
		//event.setLowPriorityValue();
        if(message.getMessageType() == MessageType.CONFIRMATION) {
            event.setEventType(EventType.CONFIRMATION);

        }
        return  null;
	}
}
