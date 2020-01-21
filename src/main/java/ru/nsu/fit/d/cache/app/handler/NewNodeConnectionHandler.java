package ru.nsu.fit.d.cache.app.handler;

import ru.nsu.fit.d.cache.app.model.Address;
import ru.nsu.fit.d.cache.app.model.AppContext;
import ru.nsu.fit.d.cache.app.util.SenderService;
import ru.nsu.fit.d.cache.channel.Message;
import ru.nsu.fit.d.cache.queue.event.Event;
import ru.nsu.fit.d.cache.queue.event.EventType;

public class NewNodeConnectionHandler extends EventHandler {
	
	private SenderService senderService;
	
	public NewNodeConnectionHandler() {
		this.senderService = new SenderService();
	}
	
	@Override
	public void handle(AppContext context, Event event) {
		
		if (!context.isWriter()) {
			// TODO: 18.01.20 тут можно будет сообщить новой ноде writer url
			return;
		}
		
		Message message = buildSubscribeMessage(context, event.getFromHost(), event.getFromPort());
		
		senderService.sendMessage(context, message);
		
		senderService.sendConfirmation(context, event);
	}
	
	private Message buildSubscribeMessage(AppContext context, String destinationHost, int destinationPort) {
		
		Address multicastAddress = context.getMulticastAddress();
		Address srcAddress = context.getSrcAddress();
		
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
}
