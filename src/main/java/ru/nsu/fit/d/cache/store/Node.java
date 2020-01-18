package ru.nsu.fit.d.cache.store;

import lombok.Getter;
import lombok.Setter;
import ru.nsu.fit.d.cache.channel.Message;
import ru.nsu.fit.d.cache.channel.MessageType;
import ru.nsu.fit.d.cache.channel.Receiver;
import ru.nsu.fit.d.cache.channel.Sender;
import ru.nsu.fit.d.cache.event.Event;
import ru.nsu.fit.d.cache.event.EventQueue;
import ru.nsu.fit.d.cache.event.EventType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class Node<T> {
	
	private static final long EVENT_TIMEOUT = 500;
	
	private static final TimeUnit EVENT_TIMEOUT_TIME_UNIT = TimeUnit.MILLISECONDS;
	
	private Sender sender;
	
	private Receiver receiver;
	
	private EventQueue<T> eventQueue;
	
	private Map<String, StoreValue<T>> store;
	
	private boolean isWriter;
	
	private String writerUrl;
	
	private long currentChangeId;
	
	private String srcUrl;
	
	private String multicastUrl;
	
	public Node(int port, String writerUrl, String receiverMulticastUrl, String multicastUrl) {
		
		EventQueue<T> eventQueue = new EventQueue<T>();
		
		this.eventQueue = eventQueue;
		this.sender = new Sender();
		this.receiver = new Receiver(port, receiverMulticastUrl, eventQueue);
		this.store = new HashMap<String, StoreValue<T>>();
		this.writerUrl = writerUrl;
		this.srcUrl = "localhost:" + port;
		this.multicastUrl = multicastUrl;
	}
	
	public void run() {
		
		
		while(true) {
			
			try {
				Event<T> event = eventQueue.poll(EVENT_TIMEOUT, EVENT_TIMEOUT_TIME_UNIT);
				
				handleEvent(event);
				
				
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
		// TODO: 18.01.20 Запустить в отдельном потоке receiver, чтобы слушал входящие запросы
	}
	
	private void handleEvent(Event<T> event) {
		
		EventType eventType = event.getEventType();
		
		// TODO: 18.01.20 фабрика
		
		if (EventType.WRITE_TO_STORE == eventType) {
			
			writeToStore(event);
			
		} else if (EventType.NEW_CONNECTION == eventType) {
			
			handleNewNodeConnection(event);
			
		} else if (EventType.CONFIRMATION == eventType) {
			
			handleConfirmation(event);
		}
	}
	
	private void writeToStore(Event<T> event) {
		
		if (isIrrelevantData(event)) {
			return;
		}
		
		String key = event.getKey();
		T value = event.getValue();
		long changeId = getNewChangeId();
		
		StoreValue<T> storeValue = new StoreValue<T>(value, changeId);
		
		store.put(key, storeValue);
		
		if (isWriter()) {
			initBroadcast(key, value, changeId);
		} else {
			sendToWriter(key, value, changeId);
		}
	}
	
	private void handleNewNodeConnection(Event<T> event) {
		
		if (!isWriter()) {
			// TODO: 18.01.20 тут можно будет сообщить новой ноде writer url
			return;
		}
		
		Message message = buildSubscribeMessage();
		sender.send(event.getFromUrl(), message);
	}
	
	private void handleConfirmation(Event<T> event) {
		
		// TODO: 18.01.20 В зависимости от контекста.
		//  Например, в случае подтверждения подписки начать обход стора и его отправку
	}
	
	private void initBroadcast(String key, T value, long changeId) {
		
		// TODO: 18.01.20
	}
	
	private void sendToWriter(String key, T value, long changeId) {
		
		Message<T> message = buildPutMessage(key, value, changeId);
		sender.send(writerUrl, message);
	}
	
	private Message buildSubscribeMessage() {
		return Message.builder()
				.messageType(MessageType.SUBSCRIBE)
				.freeText(multicastUrl)
				.srcUrl(srcUrl)
				.build();
	}
	
	private Message<T> buildPutMessage(String key, T value, long changeId) {
		
		return new Message<T>(MessageType.PUT, key, value, changeId, null, false, srcUrl, null);
	}
	
	private boolean isIrrelevantData(Event event) {
		
		return event.isLowPriorityValue() && store.containsKey(event.getKey());
	}
	
	private long getNewChangeId() {
		return ++currentChangeId;
	}
}
