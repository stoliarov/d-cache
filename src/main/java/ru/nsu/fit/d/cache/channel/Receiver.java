package ru.nsu.fit.d.cache.channel;

import lombok.AllArgsConstructor;
import ru.nsu.fit.d.cache.queue.event.Event;
import ru.nsu.fit.d.cache.queue.event.EventQueue;
import ru.nsu.fit.d.cache.queue.event.EventType;
import ru.nsu.fit.d.cache.tools.Serializer;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

@AllArgsConstructor
public class Receiver {

	private static final int WAIT_TIMEOUT = 200;

	private EventQueue eventQueue;

	private int port;

	public Message receive() {
		return null;
	}
/**
 *	возвращает поток, который принимает от врайтера старые данные
 * */
	public Thread getPeerToPeerListener() {
		return new Thread(() -> {
			try {
				//create socket
				DatagramSocket datagramSocket = new DatagramSocket(port);
				byte[] buf = new byte[4096];
				DatagramPacket receivePacket = new DatagramPacket(buf, 0, buf.length);

				receiveMessage(datagramSocket, receivePacket);
			} catch (UnknownHostException e) {
			System.err.println("multicast url unknown");
		} catch (IOException e) {
			System.err.println("impossible create multicast socket");
		}
		});
	}

	private void receiveMessage(DatagramSocket datagramSocket, DatagramPacket receivePacket) throws IOException {
		while (!Thread.currentThread().isInterrupted()) {
			//receiving
			datagramSocket.setSoTimeout(WAIT_TIMEOUT);
			try {
				datagramSocket.receive(receivePacket);
			} catch (SocketTimeoutException e) {
				continue;
			}
			//parse data to message-object
			byte[] data = receivePacket.getData();
			String strMessage = Arrays.toString(data);
			Message message = Serializer.deserializeMessage(strMessage);
			//create new event
			Event event = createEventByMessage(message, receivePacket);
			eventQueue.add(event);
		}
	}

	/**
 * возвращает поток, который слушает мультикаст
 * */
	public Thread getMulticastListener(String url, int port) {
		return new Thread(() -> {
			try {
				//create multicast socket and join to group
				InetAddress group = InetAddress.getByName(url);
				MulticastSocket receiveMulticsSocket = new MulticastSocket(port);
				receiveMulticsSocket.joinGroup(group);
				byte[] buf = new byte[4096];
				DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

				receiveMessage(receiveMulticsSocket, receivePacket);
			} catch (UnknownHostException e) {
				System.err.println("multicast url unknown");
			} catch (IOException e) {
				System.err.println("impossible create multicast socket");
			}
		});
	}

	private Event createEventByMessage(Message message, DatagramPacket packet) {
		Event event = new Event();
		event.setChangeId(message.getChangeId());

		if (message.isMulticast()) {
			event.setFromHost(message.getSrcHost());
			event.setFromPort(message.getSrcPort());
		}
		else {
			event.setFromHost(packet.getAddress().getHostName());
			event.setFromPort(packet.getPort());
		}

		event.setKey(message.getKey());
		event.setLowPriorityValue(message.isLowPriorityValue());
		event.setRequestContext(message.getRequestContext());
		event.setSerializedValue(message.getSerializedValue());

		EventType eventType;

		switch (message.getMessageType()) {
			case CONFIRMATION:
				eventType = EventType.CONFIRMATION;
				break;
			case PUT:
				eventType = EventType.WRITE_TO_STORE;
				break;
			case GET:
				eventType = EventType.NEW_CONNECTION;
				break;
			case SUBSCRIBE:
				eventType = EventType.GOT_MULTICAST;
				break;
			default:
				eventType = EventType.UNKNOWN;
		}

		event.setEventType(eventType);

		return event;
	}
}
