package ru.nsu.fit.d.cache.app.handler;

import ru.nsu.fit.d.cache.app.model.Address;
import ru.nsu.fit.d.cache.app.model.AppContext;
import ru.nsu.fit.d.cache.app.util.SenderService;
import ru.nsu.fit.d.cache.queue.event.Event;

public class SubscribeHandler extends EventHandler {
	
	private SenderService senderService;
	
	public SubscribeHandler() {
		this.senderService = new SenderService();
	}
	
	@Override
	public void handle(AppContext context, Event event) {
		
		if (context.isWriter()) {
			return;
		}
		
		String multicastHost = event.getMulticastHost();
		int multicastPort = event.getMulticastPort();
		
		Address multicastAddress = new Address(multicastHost, multicastPort);
		
		context.setMulticastAddress(multicastAddress);
		
		context.getReceiver()
				.getMulticastListener(multicastAddress.getHost(), multicastAddress.getPort())
				.start();
		
		context.getSender().initMulticast(
				event.getRequestContext().getMulticastHost(),
				event.getRequestContext().getMulticastPort()
		);
		
		senderService.sendConfirmation(context, event);
	}
}
