package ru.nsu.fit.d.cache.channel;

import ru.nsu.fit.d.cache.tools.Serializer;
import ru.nsu.fit.d.cache.queue.message.MessagesToSendQueue;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Sender implements Runnable {

	private static final long WAIT_TIMEOUT = 300;
	private MessagesToSendQueue messagesToSendQueue;

	private MulticastSocket multicastSocket;

	private DatagramSocket socket;

	private int multicastPort;

	private InetAddress multicastAddress;

	public Sender(MessagesToSendQueue messagesToSendQueue) throws IOException {
		this.messagesToSendQueue = messagesToSendQueue;

		int port = (int) (Math.random() * (64000 - 5000)) + 5000;
		this.socket = new DatagramSocket(port);
	}

	public void initMulticast(String multicastHost, int multicastPort) {
		try {
			this.multicastAddress = InetAddress.getByName(multicastHost);
			this.multicastSocket = new MulticastSocket(multicastPort);
			this.multicastPort = multicastPort;
			this.multicastSocket.joinGroup(this.multicastAddress);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		int port = (int) (Math.random() * (64000 - 5000)) + 5000;

		while(true) {
			try {
				send(messagesToSendQueue.take());
				Thread.sleep(WAIT_TIMEOUT);
			} catch (Exception e) {
				socket.close();
				multicastSocket.close();

				throw new RuntimeException(e);
			}
		}
	}

	private void send(Message message) throws IOException {

		byte[] bytes = getBytes(message);

		if (message.isMulticast()) {

			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, multicastAddress, multicastPort);

			try {
				multicastSocket.send(packet);
				message.send(System.currentTimeMillis());

			} catch (IOException e) {
				System.out.println("Error occured while sending multicast");
			}
		}
		else {

			InetAddress destinationAddress = InetAddress.getByName(message.getDestinationHost());
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, destinationAddress, message.getDestinationPort());

			socket.send(packet);
			message.send(System.currentTimeMillis());
		}

	}

	private byte[] getBytes(Message message) {
		String jsonString = Serializer.getJsonString(message);
		return jsonString.getBytes(StandardCharsets.UTF_8);
	}
}
