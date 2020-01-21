package ru.nsu.fit.d.cache.store;

import lombok.*;
import ru.nsu.fit.d.cache.channel.*;
import ru.nsu.fit.d.cache.console.ConsoleReader;
import ru.nsu.fit.d.cache.queue.event.Event;
import ru.nsu.fit.d.cache.queue.event.EventQueue;
import ru.nsu.fit.d.cache.queue.event.EventType;
import ru.nsu.fit.d.cache.queue.message.MessagesToSendQueue;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class Node<T> implements Runnable {

	private static final int CONFIRMATION_TIMEOUT = 2000;

	private static final int CHECK_CONFIRMATION_TIMEOUT = 500;

	private Sender sender;

	private Receiver receiver;

	private EventQueue eventQueue;

	private MessagesToSendQueue messagesToSendQueue;

	private Map<String, StoreValue> store;

	private Map<Address, String> knownNodes;

	private Map<Address, Message> expectedConfirmation;
	
	private Map<Address, Iterator<Map.Entry<String, StoreValue>>> activeStoreIterators;

	private Timer confirmationTimer;

	private boolean isWriter;

	private long currentChangeId;

	private Address writerAddress;

	private Address srcAddress;

	private Address multicastAddress;
	
	// reader проставляет в true после выгрузки стора врайтером
	private boolean initCompleted;
	
	public Node(int port, String writerHost, int writerPort, String multicastHost, int multicastPort, boolean isWriter)
			throws IOException {

		EventQueue eventQueue = new EventQueue();
		MessagesToSendQueue messagesToSendQueue = new MessagesToSendQueue();

		this.eventQueue = eventQueue;
		this.messagesToSendQueue = messagesToSendQueue;
		this.sender = new Sender(messagesToSendQueue);
		this.receiver = new Receiver(eventQueue, port);
		this.store = new HashMap<>();
		this.knownNodes = new ConcurrentHashMap<>();
		this.expectedConfirmation = new ConcurrentHashMap<>();
		this.activeStoreIterators = new HashMap<>();
		this.srcAddress = new Address("localhost", port);
		this.multicastAddress = new Address(multicastHost, multicastPort);
		this.writerAddress = new Address(writerHost, writerPort);
		this.isWriter = isWriter;

		if (isWriter) {
			sender.initMulticast(multicastHost, multicastPort);
		}
	}

	@Override
	public void run() {

		if (isWriter()) {
			startConfirmationTimeoutChecking();
		}
		//run threads
		new Thread(sender).start();

		if (isWriter()) {
			receiver
					.getMulticastListener(multicastAddress.getHost(), multicastAddress.getPort())
					.start();
		}

		new Thread(new ConsoleReader(eventQueue))
				.start();

		while(true) {

			try {
				Event event = eventQueue.take();

				handleEvent(event);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleEvent(Event event) {

		EventType eventType = event.getEventType();

		// TODO: 18.01.20 фабрика

		switch (eventType) {
			case PUT: {
				writeToStore(event);
				break;
			}
			case NEW_CONNECTION: {
				handleNewNodeConnection(event);
				break;
			}
			case CONFIRMATION: {
				handleConfirmation(event);
				break;
			}
			case SUBSCRIBE:
				handleSubscribe(event);
				break;
			case STORE_SHARING:
				handleStoreSharing(event);
				break;
			case END_OF_STORE:
				handleEndOfStore(event);
				break;
		}
	}
	
	private void handleStoreSharing(Event event) {
		
		String key = event.getKey();
		
		if (!store.containsKey(key)) {
			store.put(key, new StoreValue(event.getSerializedValue(), event.getChangeId()));
		}
		
		sendConfirmation(event);
	}
	
	private void handleEndOfStore(Event event) {
		
		this.initCompleted = true;
		
		sendConfirmation(event);
	}
	
	private void handleSubscribe(Event event) {
		
		if (isWriter()) {
			return;
		}
		
		String multicastHost = event.getMulticastHost();
		int multicastPort = event.getMulticastPort();
		multicastAddress = new Address(multicastHost, multicastPort);
		
		receiver.getMulticastListener(multicastAddress.getHost(), multicastAddress.getPort())
				.start();
		sender.initMulticast(event.getRequestContext().getMulticastHost(), event.getRequestContext().getMulticastPort());
		
		sendConfirmation(event);
	}

	private void writeToStore(Event event) {

		if (isIrrelevantData(event)) {
			return;
		}

		String key = event.getKey();
		String value = event.getSerializedValue();
		long changeId = getNewChangeId();

		StoreValue storeValue = new StoreValue(value, changeId);

		store.put(key, storeValue);

		if (isWriter()) {
			if (event.getFromHost() != null) {
				sendConfirmation(event);
			}
			initBroadcast(key, value, changeId);
		} else {
			sendToWriter(key, value, changeId);
		}
	}

	private void handleNewNodeConnection(Event event) {

		if (!isWriter()) {
			// TODO: 18.01.20 тут можно будет сообщить новой ноде writer url
			return;
		}

		Message message = buildSubscribeMessage(event.getFromHost(), event.getFromPort());

		messagesToSendQueue.offer(message);
		expectForConfirmation(message);

		sendConfirmation(event);
	}

	private void handleConfirmation(Event event) {
		
		EventType contextEventType = Optional.of(event)
				.map(Event::getRequestContext)
				.map(RequestContext::getMessageType)
				.orElse(null);
		
		Address srcAddress = new Address(event.getFromHost(), event.getFromPort());
		
		if (isWriter && contextEventType == EventType.SUBSCRIBE) {
			
			startStoreSharing(srcAddress);
			sendNextValueFromStore(srcAddress);
			
		} else if (isWriter() && contextEventType == EventType.STORE_SHARING) {
			
			sendNextValueFromStore(srcAddress);
		}
		
		// TODO: 18.01.20 В зависимости от контекста.
		//  Например, в случае подтверждения подписки начать обход стора и его отправку
	}
	
	/**
	 * Инициирует рассылку всех данных из стора.
	 *
	 * @param destinationAddress получатель рассылки
	 */
	private void startStoreSharing(Address destinationAddress) {
		
		Iterator<Map.Entry<String, StoreValue>> iterator = store.entrySet().iterator();
		
		Address otherNodeAddress = new Address(destinationAddress.getHost(), destinationAddress.getPort());
		
		activeStoreIterators.put(otherNodeAddress, iterator);
	}
	
	/**
	 * Использует writer.
	 * Отправляет следующее сообщение для данного ридера в рамках рассылки стора при инициализации новой ноды
	 *
	 * @param destinationAddress адрес ридера, для которого производится рассылка
	 */
	private void sendNextValueFromStore(Address destinationAddress) {
		
		String receiverHost = destinationAddress.getHost();
		int receiverPort = destinationAddress.getPort();
		
		Iterator<Map.Entry<String, StoreValue>> iterator = activeStoreIterators.get(destinationAddress);
		
		if (!iterator.hasNext()) {
			activeStoreIterators.remove(destinationAddress);
			
			Message message = Message.builder()
					.eventType(EventType.END_OF_STORE)
					.srcHost(srcAddress.getHost())
					.srcPort(srcAddress.getPort())
					.destinationHost(receiverHost)
					.destinationPort(receiverPort)
					.build();
			
			messagesToSendQueue.offer(message);
			expectForConfirmation(message);
		}
		
		Map.Entry<String, StoreValue> storeEntry = iterator.next();
		StoreValue storeValue = storeEntry.getValue();
		
		Message message = Message.builder()
				.key(storeEntry.getKey())
				.serializedValue(storeValue.getSerializedValue())
				.changeId(storeValue.getChangeId())
				.eventType(EventType.STORE_SHARING)
				.isLowPriorityValue(true)
				.srcHost(srcAddress.getHost())
				.srcPort(srcAddress.getPort())
				.destinationHost(receiverHost)
				.destinationPort(receiverPort)
				.build();
		
		messagesToSendQueue.offer(message);
		expectForConfirmation(message);
	}

	private void sendConfirmation(Event event) {
		Message confirmationMessage = buildConfirmationMessage(event);

		messagesToSendQueue.offer(confirmationMessage);
	}

	private Message buildConfirmationMessage(Event event) {
		
		Message confirmationMessage = new Message();
		
		String key = event.getKey();
		
		Long changeId = key == null && store.containsKey(key) ? store.get(key).getChangeId() : event.getChangeId();
		
		RequestContext requestContext = new RequestContext();
		// TODO: 22.01.20 set type
		//		requestContext.setEventType(event.ge);
		requestContext.setSrcHost(event.getFromHost());
		requestContext.setSrcPort(event.getFromPort());
		requestContext.setChangeId(changeId);
		
		confirmationMessage.setEventType(EventType.CONFIRMATION);
		confirmationMessage.setDestinationHost(event.getFromHost());
		confirmationMessage.setDestinationPort(event.getFromPort());
		confirmationMessage.setSrcHost(srcAddress.getHost());
		confirmationMessage.setSrcPort(srcAddress.getPort());
		confirmationMessage.setRequestContext(requestContext);
		confirmationMessage.setChangeId(changeId);

		return confirmationMessage;
	}

	private void initBroadcast(String key, String value, long changeId) {

		Message message = buildPutMessage(key, value, changeId, true, multicastAddress);

		messagesToSendQueue.offer(message);

		knownNodes.forEach(((address, s) -> {

			Address destinationAddress = new Address(address.getHost(), address.getPort());
			expectedConfirmation.put(destinationAddress, message);
		}));
	}

	private void sendToWriter(String key, String value, long changeId) {

		Message message = buildPutMessage(key, value, changeId, false, writerAddress);

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

	private void expectForConfirmation(Message message) {

		Address destinationAddress = new Address(message.getDestinationHost(), message.getDestinationPort());
		expectedConfirmation.put(destinationAddress, message);
	}

	private Message buildSubscribeMessage(String destinationHost, int destinationPort) {

		Message message = new Message();

		message.setEventType(EventType.SUBSCRIBE);
		message.setMulticastHost(multicastAddress.getHost());
		message.setMulticastPort(multicastAddress.getPort());
		message.setSrcHost(srcAddress.getHost());
		message.setSrcPort(srcAddress.getPort());
		message.setDestinationHost(destinationHost);
		message.setDestinationPort(destinationPort);

		return message;
	}

	private Message buildPutMessage(String key, String value, long changeId, boolean isMulticast,
	                                   Address destinationAddress) {

		Message message = new Message();

		message.setEventType(EventType.PUT);
		message.setKey(key);
		message.setSerializedValue(value);
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
					activeStoreIterators.remove(key);

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
