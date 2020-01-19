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
	public Thread getPearToPearListener () {
		return new Thread(() -> {
			try {
				//create socket
				DatagramSocket datagramSocket = new DatagramSocket(port);
				byte[] buf = new byte[4096];
				DatagramPacket receivePacket = new DatagramPacket(buf, 0, buf.length);

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
			} catch (UnknownHostException e) {
			System.err.println("multicast url unknown");
		} catch (IOException e) {
			System.err.println("impossible create multicast socket");
		}
		});
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

				while(!Thread.currentThread().isInterrupted()) {
					//receiving
					receiveMulticsSocket.setSoTimeout(WAIT_TIMEOUT);
					try {
						receiveMulticsSocket.receive(receivePacket);
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
		event.setFromHost(packet.getAddress().toString());
		event.setFromPort(packet.getPort());
		event.setKey(message.getKey());
		event.setLowPriorityValue(message.isLowPriorityValue());
		event.setRequestContext(message.getRequestContext());
		event.setSerializedValue(message.getSerializedValue());

		EventType eventType = null;

		if(message.getMessageType() == MessageType.CONFIRMATION)
			eventType = EventType.CONFIRMATION;
		/*
		TODO: 20.01.20 какие значения MessageType соответств значениям EventType?
		* */
		/*if(message.getMessageType() == MessageType.)
			eventType = EventType.WRITE_TO_STORE;
		if(message.getMessageType() == MessageType.CONFIRMATION)
			eventType = EventType.NEW_CONNECTION;*/

		event.setEventType(eventType);

		return event;
	}
}
