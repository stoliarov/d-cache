package ru.nsu.fit.d.cache.store;

import lombok.*;
import ru.nsu.fit.d.cache.channel.Message;
import ru.nsu.fit.d.cache.channel.MessageType;
import ru.nsu.fit.d.cache.channel.Receiver;
import ru.nsu.fit.d.cache.channel.Sender;
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
	
	private Map<Address, String> knownNodes;
	
	private Map<Address, Message<T>> expectedConfirmation;
	
	private Timer confirmationTimer;
	
	private boolean isWriter;
	
	private long currentChangeId;
	
	private Address writerAddress;
	
	private Address srcAddress;
	
	private Address multicastAddress;
	
	public Node(int port, String writerHost, int writerPort, String multicastHost, int multicastPort)
			throws IOException {
		
		EventQueue<T> eventQueue = new EventQueue<>();
		MessagesToSendQueue<T> messagesToSendQueue = new MessagesToSendQueue<>();
		
		this.eventQueue = eventQueue;
		this.messagesToSendQueue = messagesToSendQueue;
		this.sender = new Sender<>(messagesToSendQueue, multicastHost, multicastPort);
		this.receiver = new Receiver(eventQueue, port);
		this.store = new HashMap<>();
		this.knownNodes = new ConcurrentHashMap<>();
		this.expectedConfirmation = new ConcurrentHashMap<>();
		this.srcAddress = new Address("localhost", port);
		this.multicastAddress = new Address(multicastHost, multicastPort);
		this.writerAddress = new Address(writerHost, writerPort);
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
		
		Message<T> message = buildSubscribeMessage(event.getFromHost(), event.getFromPort());
		
		messagesToSendQueue.offer(message);
		expectForConfirmation(message);
	}
	
	private void handleConfirmation(Event<T> event) {
		
		// TODO: 18.01.20 В зависимости от контекста.
		//  Например, в случае подтверждения подписки начать обход стора и его отправку
	}
	
	private void initBroadcast(String key, T value, long changeId) {
		
		Message<T> message = buildPutMessage(key, value, changeId, true, multicastAddress);
		
		messagesToSendQueue.offer(message);
		
		knownNodes.forEach(((address, s) -> {
			
			Address destinationAddress = new Address(address.getHost(), address.getPort());
			expectedConfirmation.put(destinationAddress, message);
		}));
	}
	
	private void sendToWriter(String key, T value, long changeId) {
		
		Message<T> message = buildPutMessage(key, value, changeId, false, writerAddress);
		
		messagesToSendQueue.offer(message);
		expectForConfirmation(message);
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
	
	private void expectForConfirmation(Message<T> message) {
		
		Address destinationAddress = new Address(message.getDestinationHost(), message.getDestinationPort());
		expectedConfirmation.put(destinationAddress, message);
	}
	
	private Message<T> buildSubscribeMessage(String destinationHost, int destinationPort) {
		
		Message<T> message = new Message<>();
		
		message.setMessageType(MessageType.SUBSCRIBE);
		message.setFreeText(multicastAddress.getHost() + ":" + multicastAddress.getPort());
		message.setSrcHost(srcAddress.getHost());
		message.setSrcPort(srcAddress.getPort());
		message.setDestinationHost(destinationHost);
		message.setDestinationPort(destinationPort);
		
		return message;
	}
	
	private Message<T> buildPutMessage(String key, T value, long changeId, boolean isMulticast,
	                                   Address destinationAddress) {
		
		Message<T> message = new Message<>();
		
		message.setMessageType(MessageType.PUT);
		message.setKey(key);
		message.setValue(value);
		message.setChangeId(changeId);
		message.setSrcHost(srcAddress.getHost());
		message.setSrcPort(srcAddress.getPort());
		message.setMulticast(isMulticast);
		message.setDestinationHost(destinationAddress.getHost());
		message.setDestinationPort(destinationAddress.getPort());
		
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
				
				// TODO: 19.01.20 обработать, если не достучались до Writer
				
				if (System.currentTimeMillis() - message.getSendingTime() > CONFIRMATION_TIMEOUT * 3) {
					
					Address key = new Address(message.getDestinationHost(), message.getDestinationPort());
					knownNodes.remove(key);
					
				} else if (System.currentTimeMillis() - message.getSendingTime() > CONFIRMATION_TIMEOUT) {
					messagesToSendQueue.offer(message);
				}
			});
		}
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	private static class Address {
		
		private String host;
		
		private int port;
	}
}
