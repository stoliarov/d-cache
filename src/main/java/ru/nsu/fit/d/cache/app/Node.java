package ru.nsu.fit.d.cache.app;

import lombok.Getter;
import lombok.Setter;
import ru.nsu.fit.d.cache.app.model.Address;
import ru.nsu.fit.d.cache.app.model.AppContext;
import ru.nsu.fit.d.cache.console.ConsoleReader;
import ru.nsu.fit.d.cache.queue.event.Event;
import ru.nsu.fit.d.cache.queue.event.EventQueue;
import ru.nsu.fit.d.cache.queue.event.EventType;
import ru.nsu.fit.d.cache.tools.EventHandlerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

@Getter
@Setter
public class Node<T> implements Runnable {

	private static final int CONFIRMATION_TIMEOUT = 2000;

	private static final int CHECK_CONFIRMATION_TIMEOUT = 500;
	
	private AppContext context;
	
	private EventQueue eventQueue;
	
	private Timer confirmationTimer;
	
	public Node(int port, String writerHost, int writerPort, String multicastHost, int multicastPort, boolean isWriter)
			throws IOException {
		
		this.context = new AppContext(port, writerHost, writerPort, multicastHost, multicastPort, isWriter);
		this.eventQueue = new EventQueue();
	}

	@Override
	public void run() {

		if (context.isWriter()) {
			startConfirmationTimeoutChecking();
			Address multicastAddress = context.getMulticastAddress();
			context.getReceiver().getMulticastListener(multicastAddress.getHost(), multicastAddress.getPort())
					.start();
		}
		
		new Thread(context.getSender()).start();
		new Thread(new ConsoleReader(eventQueue)).start();

		while(true) {

			try {
				Event event = eventQueue.take();
				
				EventType eventType = event.getEventType();
				
				EventHandlerFactory.getInstance().getHandler(eventType).handle(context, event);

			} catch (InterruptedException | DatatypeConfigurationException | ReflectiveOperationException e) {
				e.printStackTrace();
			}
		}
	}

	private void startConfirmationTimeoutChecking() {

		confirmationTimer = new Timer();
		confirmationTimer.schedule(new ConfirmationControl(), CHECK_CONFIRMATION_TIMEOUT, CHECK_CONFIRMATION_TIMEOUT);
	}

	private class ConfirmationControl extends TimerTask {

		@Override
		public void run() {

			context.getExpectedConfirmation().forEach((id, message) -> {

				if (!message.isSent()) {
					return;
				}

				// TODO: 19.01.20 обработать, если не достучались до Writer

				if (System.currentTimeMillis() - message.getSendingTime() > CONFIRMATION_TIMEOUT * 3) {

					Address key = new Address(message.getDestinationHost(), message.getDestinationPort());
					context.getKnownNodes().remove(key);
					context.getActiveStoreIterators().remove(key);

				} else if (System.currentTimeMillis() - message.getSendingTime() > CONFIRMATION_TIMEOUT) {
					context.getMessagesToSendQueue().offer(message);
				}
			});
		}
	}
}
