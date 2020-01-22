package ru.nsu.fit.d.cache.app.model;

import lombok.Data;
import ru.nsu.fit.d.cache.channel.Message;
import ru.nsu.fit.d.cache.channel.Receiver;
import ru.nsu.fit.d.cache.channel.Sender;
import ru.nsu.fit.d.cache.queue.event.EventQueue;
import ru.nsu.fit.d.cache.queue.message.MessagesToSendQueue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class AppContext {
	
	private Sender sender;
	
	private Receiver receiver;
	
	private MessagesToSendQueue messagesToSendQueue;
	
	private Map<String, StoreValue> store;
	
	private Map<Address, String> knownNodes;
	
	private Map<Address, Message> expectedConfirmation;
	
	private Map<Address, Iterator<Map.Entry<String, StoreValue>>> activeStoreIterators;
	
	private boolean isWriter;
	
	private long currentChangeId;
	
	private Address writerAddress;
	
	private Address srcAddress;
	
	private Address multicastAddress;

	private int senderPort;

	private EventQueue eventQueue;
	
	// reader проставляет в true после выгрузки стора врайтером
	private boolean initCompleted;
	
	public AppContext(int senderPort, int receiverPort, String writerHost, int writerPort,
	                  String multicastHost, int multicastPort, boolean isWriter) throws IOException {
		
		this.eventQueue = new EventQueue();
		MessagesToSendQueue messagesToSendQueue = new MessagesToSendQueue();
		
		this.messagesToSendQueue = messagesToSendQueue;
		this.srcAddress = new Address("localhost", receiverPort);
		this.receiver = new Receiver(eventQueue, receiverPort);
		this.sender = new Sender(messagesToSendQueue, senderPort);
		this.store = new HashMap<>();
		this.knownNodes = new ConcurrentHashMap<>();
		this.expectedConfirmation = new ConcurrentHashMap<>();
		this.activeStoreIterators = new HashMap<>();
		this.multicastAddress = new Address(multicastHost, multicastPort);
		this.writerAddress = new Address(writerHost, writerPort);
		this.isWriter = isWriter;

		if (isWriter) {
			sender.initMulticast(multicastHost, multicastPort);
		}
	}
}
