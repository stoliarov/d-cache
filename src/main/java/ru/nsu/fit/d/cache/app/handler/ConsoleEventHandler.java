package ru.nsu.fit.d.cache.app.handler;

import ru.nsu.fit.d.cache.app.model.Address;
import ru.nsu.fit.d.cache.app.model.AppContext;
import ru.nsu.fit.d.cache.app.model.StoreValue;
import ru.nsu.fit.d.cache.app.util.SenderService;
import ru.nsu.fit.d.cache.channel.Message;
import ru.nsu.fit.d.cache.queue.event.Event;
import ru.nsu.fit.d.cache.queue.event.EventType;

public class ConsoleEventHandler extends EventHandler {

	private SenderService senderService;

	public ConsoleEventHandler() {
		this.senderService = new SenderService();
	}
	
	@Override
	public void handle(AppContext context, Event event) {
		
		String key = event.getKey();
		String value = event.getSerializedValue();
		long changeId = getNewChangeId(context);
		
		StoreValue storeValue = new StoreValue(value, changeId);
		
		context.getStore().put(key, storeValue);

		System.out.println("Event From console");

		if (context.isWriter()) {

			System.out.println("Send broadcast (PUT)");

			senderService.initBroadcast(context, key, value, changeId);
		} else {

			Address srcAddress = context.getSrcAddress();
			Address destinationAddress = context.getWriterAddress();

			Message message = Message.builder()
					.eventType(EventType.PUT)
					.key(key)
					.serializedValue(value)
					.changeId(changeId)
					.destinationHost(destinationAddress.getHost())
					.destinationPort(destinationAddress.getPort())
					.srcHost(srcAddress.getHost())
					.srcPort(srcAddress.getPort())
					.build();

			System.out.println("rider send message to writer (PUT)");

			senderService.sendMessage(context, message);
		}
	}

	private long getNewChangeId(AppContext context) {

		long newChangeId = context.getCurrentChangeId() + 1;
		context.setCurrentChangeId(newChangeId);

		return newChangeId;
	}
}
