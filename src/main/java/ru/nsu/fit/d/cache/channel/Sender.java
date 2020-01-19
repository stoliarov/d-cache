package ru.nsu.fit.d.cache.channel;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;
import ru.nsu.fit.d.cache.tools.Serializer;
import ru.nsu.fit.d.cache.queue.message.MessagesToSendQueue;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Sender<T> implements Runnable {

	private MessagesToSendQueue<T> messagesToSendQueue;

	private MulticastSocket multicastSocket;

	private DatagramSocket socket;

	private int multicastPort;

	private InetAddress multicastAddress;

	public Sender(MessagesToSendQueue<T> messagesToSendQueue, String multicastAddress, int multicastPort) throws IOException {
		this.messagesToSendQueue = messagesToSendQueue;
		this.multicastAddress = InetAddress.getByName(multicastAddress);
		this.multicastPort = multicastPort;
		this.multicastSocket = new MulticastSocket(multicastPort);
		this.multicastSocket.joinGroup(this.multicastAddress);

		int port = (int) (Math.random() * (64000 - 5000)) + 5000;
		this.socket = new DatagramSocket(port);
	}

	@Override
	public void run() {
		int port = (int) (Math.random() * (64000 - 5000)) + 5000;

		try  {
			send(messagesToSendQueue.take());

		} catch (Exception e) {
			socket.close();
			multicastSocket.close();

			throw new RuntimeException(e);
		}
	}

	private void send(Message<T> message) throws IOException {

		byte[] bytes = getBytes(message);

		if (message.isMulticast()) {

			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, multicastAddress, multicastPort);

			try {
				multicastSocket.send(packet);

			} catch (IOException e) {
				System.out.println("Error occured while sending multicast");
			}
		}
		else {

			InetAddress destinationAddress = InetAddress.getByName(message.getDestinationUrl());
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, destinationAddress, message.getDestinationPort());

			socket.send(packet);
		}

	}

	private byte[] getBytes(Message<T> message) {
		String jsonString = Serializer.getJsonString(message);
		return jsonString.getBytes(StandardCharsets.UTF_8);
	}

	private void send(String url, Message request) {

		ZContext context = new ZContext();

		Socket client = context.createSocket(SocketType.REQ);

		client.connect(url);

		ZMsg message = new ZMsg();
		message.append("some string");

		message.send(client);

		context.destroySocket(client);
	}
}
