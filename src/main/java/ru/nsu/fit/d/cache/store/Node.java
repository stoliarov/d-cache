package ru.nsu.fit.d.cache.store;

import lombok.Getter;
import lombok.Setter;
import ru.nsu.fit.d.cache.channel.Message;
import ru.nsu.fit.d.cache.channel.MessageType;
import ru.nsu.fit.d.cache.channel.Receiver;
import ru.nsu.fit.d.cache.channel.Sender;
import ru.nsu.fit.d.cache.console.Data;
import ru.nsu.fit.d.cache.queue.event.Event;
import ru.nsu.fit.d.cache.queue.event.EventQueue;
import ru.nsu.fit.d.cache.queue.event.EventType;
import ru.nsu.fit.d.cache.queue.message.MessagesToSendQueue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class Node<T> {
	
	private static final int CONFIRMATION_TIMEOUT = 2000;
	
	private static final int CHECK_CONFIRMATION_TIMEOUT = 500;
	
	private Sender<T> sender;
	
	private Receiver receiver;
	
	private EventQueue<T> eventQueue;
	
	private MessagesToSendQueue<T> messagesToSendQueue;
	
	private Map<String, StoreValue<T>> store;
	
	private Map<String, String> knownNodes;
	
	private Map<String, Message<T>> expectedConfirmation;
	
	private Timer confirmationTimer;
	
	private boolean isWriter;
	
	private String writerUrl;
	
	private long currentChangeId;
	
	private String srcUrl;
	
	private String multicastUrl;
	
	public Node(int port, int multicastPort, String writerUrl, String multicastUrl) throws IOException {
		
		EventQueue<T> eventQueue = new EventQueue<>();
		MessagesToSendQueue<T> messagesToSendQueue = new MessagesToSendQueue<>();
		
		this.eventQueue = eventQueue;
		this.messagesToSendQueue = messagesToSendQueue;
		this.sender = new Sender<T>(messagesToSendQueue, multicastUrl, multicastPort);
		this.receiver = new Receiver(eventQueue, port);
		this.store = new HashMap<>();
		this.knownNodes = new ConcurrentHashMap<>();
		this.expectedConfirmation = new ConcurrentHashMap<>();
		this.writerUrl = writerUrl;
		this.srcUrl = "localhost:" + port;
		this.multicastUrl = multicastUrl;
	}
	
	public void run() {
		
		if (isWriter()) {
			startConfirmationTimeoutChecking();
		}
		
		while(true) {
			
			try {
				Event<T> event = eventQueue.take();
				
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
		
		StoreValue<T> storeValue = new StoreValue<>(value, changeId);
		
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
		
		Message<T> message = buildSubscribeMessage();
		message.setDestinationUrl(event.getFromUrl());
		
		messagesToSendQueue.offer(message);
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
		message.setDestinationUrl(writerUrl);
		messagesToSendQueue.offer(message);
	}
	
	private void startConfirmationTimeoutChecking() {
		
		confirmationTimer = new Timer();
		
		confirmationTimer.schedule(new ConfirmationControl(), CHECK_CONFIRMATION_TIMEOUT, CHECK_CONFIRMATION_TIMEOUT);
	}
	
	private void stopConfirmationTimeoutChecking() {
		
		if (confirmationTimer != null) {
			confirmationTimer.cancel();
		}
	}
	
	private Message<T> buildSubscribeMessage() {
		
		Message<T> message = new Message<>();
		
		message.setMessageType(MessageType.SUBSCRIBE);
		message.setFreeText(multicastUrl);
		message.setSrcUrl(srcUrl);
		
		return message;
	}
	
	private Message<T> buildPutMessage(String key, T value, long changeId) {
		
		Message<T> message = new Message<>();
		
		message.setMessageType(MessageType.PUT);
		message.setKey(key);
		message.setValue(value);
		message.setChangeId(changeId);
		message.setSrcUrl(srcUrl);
		
		return message;
	}
	
	private boolean isIrrelevantData(Event event) {
		
		return event.isLowPriorityValue() && store.containsKey(event.getKey());
	}
	
	private long getNewChangeId() {
		return ++currentChangeId;
	}
	
	private class ConfirmationControl extends TimerTask {
		
		@Override
		public void run() {
			
			expectedConfirmation.forEach((id, message) -> {
				
				if (!message.isSent()) {
					return;
				}
				
				if (System.currentTimeMillis() - message.getSendingTime() > CONFIRMATION_TIMEOUT * 3) {
					knownNodes.remove(message.getSrcUrl());
					
				} else if (System.currentTimeMillis() - message.getSendingTime() > CONFIRMATION_TIMEOUT) {
					messagesToSendQueue.offer(message);
				}
			});
		}
	}
}
