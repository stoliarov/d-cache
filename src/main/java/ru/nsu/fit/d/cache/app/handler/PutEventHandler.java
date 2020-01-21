package ru.nsu.fit.d.cache.app.handler;

import ru.nsu.fit.d.cache.app.model.Address;
import ru.nsu.fit.d.cache.app.model.AppContext;
import ru.nsu.fit.d.cache.app.model.StoreValue;
import ru.nsu.fit.d.cache.app.util.SenderService;
import ru.nsu.fit.d.cache.channel.Message;
import ru.nsu.fit.d.cache.queue.event.Event;
import ru.nsu.fit.d.cache.queue.event.EventType;

public class PutEventHandler extends EventHandler {
	
	private SenderService senderService;
	
	public PutEventHandler() {
		this.senderService = new SenderService();
	}
	
	@Override
	public void handle(AppContext context, Event event) {
		
		if (isIrrelevantData(context, event)) {
			return;
		}
		
		String key = event.getKey();
		String value = event.getSerializedValue();
		long changeId = getNewChangeId(context);
		
		StoreValue storeValue = new StoreValue(value, changeId);
		
		context.getStore().put(key, storeValue);
		
		if (context.isWriter()) {
			
			if (event.getFromHost() != null) {
				senderService.sendConfirmation(context, event);
			}
			
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
			
			senderService.sendMessage(context, message);
		}
	}
	
	private boolean isIrrelevantData(AppContext context, Event event) {
		
		return event.isLowPriorityValue() && context.getStore().containsKey(event.getKey());
	}
	
	private long getNewChangeId(AppContext context) {
		
		long newChangeId = context.getCurrentChangeId() + 1;
		context.setCurrentChangeId(newChangeId);
		
		return newChangeId;
	}
}
