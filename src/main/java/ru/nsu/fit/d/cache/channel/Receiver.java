package ru.nsu.fit.d.cache.channel;

import lombok.AllArgsConstructor;
import ru.nsu.fit.d.cache.event.Event;
import ru.nsu.fit.d.cache.event.EventQueue;

import java.io.IOException;
import java.net.*;
import java.rmi.UnknownHostException;
import java.util.Arrays;

@AllArgsConstructor
public class Receiver implements Runnable {
	
	private static final int WAIT_TIMEOUT = 200;

	private String endpoint;

	private EventQueue eventQueue;
	
	private int port;

	InetAddress multicastGroup;
	MulticastSocket receiveMulticsSocket;
	byte[] multicastBuf;
	DatagramPacket multicastReceivePacket;

	public Receiver(int port, String endpoint, EventQueue eventQueue) {
		this.port = port;
		this.endpoint = endpoint;
		this.eventQueue = eventQueue;

		try {
			multicastGroup = InetAddress.getByName(endpoint);
			receiveMulticsSocket = new MulticastSocket(port);
			receiveMulticsSocket.joinGroup(multicastGroup);
			multicastBuf= new byte[1024];
			multicastReceivePacket = new DatagramPacket(multicastBuf, multicastBuf.length);
		} catch (UnknownHostException e) {
			System.out.println("Unknown Host");
		} catch (IOException e) {
			System.out.println("impossible create socket");
		}
	}

	/**
	 *получение сообщений с мультикаста
	 * */
	public Message receive() {
		
		// TODO: 18.01.20 Запускается в отдельном потоке, блокируется и ждет запросы.
		//  Кладет полученное в очередь как event. Далее из этой очереди класс Node будет выгребать запросы и реагировать

		try {
			receiveMulticsSocket.setSoTimeout(WAIT_TIMEOUT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		try {
			receiveMulticsSocket.receive(multicastReceivePacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String messageString = Arrays.toString(multicastReceivePacket.getData());
		Message message = null;
		//TODO: 19.01.20 десириализовать messageString -> message
		return message;
	}

	public void run() {
		// мультикаст сокет
		while (true) {
			Message message = receive();
			//TODO: 19.01.20 положить новый эвент согласно полученому сообщению
			Event event = null;
			eventQueue.add(event);
		}
	}


}
