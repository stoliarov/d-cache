package ru.nsu.fit.d.cache.store;

import lombok.*;
import ru.nsu.fit.d.cache.channel.Message;
import ru.nsu.fit.d.cache.channel.MessageType;
import ru.nsu.fit.d.cache.channel.Receiver;
import ru.nsu.fit.d.cache.channel.Sender;
import ru.nsu.fit.d.cache.console.ConsoleReader;
import ru.nsu.fit.d.cache.queue.event.Event;
import ru.nsu.fit.d.cache.queue.event.EventQueue;
import ru.nsu.fit.d.cache.queue.event.EventType;
import ru.nsu.fit.d.cache.queue.message.MessagesToSendQueue;
import ru.nsu.fit.d.cache.tools.Serializer;

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

	private Sender sender;

	private Receiver receiver;

	private EventQueue eventQueue;

	private MessagesToSendQueue messagesToSendQueue;

	private Map<String, StoreValue> store;

	private Map<Address, String> knownNodes;

	private Map<Address, Message> expectedConfirmation;

	private Timer confirmationTimer;

	private boolean isWriter;

	private long currentChangeId;

	private Address writerAddress;

	private Address srcAddress;

	private Address multicastAddress;

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
		this.srcAddress = new Address("localhost", port);
		this.multicastAddress = new Address(multicastHost, multicastPort);
		this.writerAddress = new Address(writerHost, writerPort);
		this.isWriter = isWriter;

		if (isWriter) {
			sender.initMulticast(multicastHost, multicastPort);
		}
	}

	public void run() {

		if (isWriter()) {
			startConfirmationTimeoutChecking();
		}
		//run threads
		new Thread(sender).start();
		receiver
				.getMulticastListener(multicastAddress.getHost(), multicastAddress.getPort())
				.start();
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
			case  WRITE_TO_STORE: {
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
		}
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
	}

	private void handleConfirmation(Event event) {

		// TODO: 18.01.20 В зависимости от контекста.
		//  Например, в случае подтверждения подписки начать обход стора и его отправку
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

		message.setMessageType(MessageType.SUBSCRIBE);
		message.setFreeText(multicastAddress.getHost() + ":" + multicastAddress.getPort());
		message.setSrcHost(srcAddress.getHost());
		message.setSrcPort(srcAddress.getPort());
		message.setDestinationHost(destinationHost);
		message.setDestinationPort(destinationPort);

		return message;
	}

	private Message buildPutMessage(String key, String value, long changeId, boolean isMulticast,
	                                   Address destinationAddress) {

		Message message = new Message();

		message.setMessageType(MessageType.PUT);
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
