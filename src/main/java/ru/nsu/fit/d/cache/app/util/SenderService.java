package ru.nsu.fit.d.cache.app.util;


import lombok.NoArgsConstructor;
import ru.nsu.fit.d.cache.app.model.Address;
import ru.nsu.fit.d.cache.app.model.AppContext;
import ru.nsu.fit.d.cache.app.model.StoreValue;
import ru.nsu.fit.d.cache.channel.Message;
import ru.nsu.fit.d.cache.channel.RequestContext;
import ru.nsu.fit.d.cache.queue.event.Event;
import ru.nsu.fit.d.cache.queue.event.EventType;

import java.util.Map;

@NoArgsConstructor
public class SenderService {
	
	public void sendConfirmation(AppContext context, Event event) {
		Message confirmationMessage = buildConfirmationMessage(context, event);
		
		context.getMessagesToSendQueue().offer(confirmationMessage);
	}
	
	public void sendMessage(AppContext context, Message message) {
		
		context.getMessagesToSendQueue().offer(message);
		
		Address destinationAddress = new Address(message.getDestinationHost(), message.getDestinationPort());
		context.getExpectedConfirmation().put(destinationAddress, message);
	}
	
	public void initBroadcast(AppContext context, String key, String value, long changeId) {
		
		Message message = buildPutMessage(context, key, value, changeId, true, context.getMulticastAddress());
		
		context.getMessagesToSendQueue().offer(message);
		
		context.getKnownNodes().forEach(((address, s) -> {
			
			Address destinationAddress = new Address(address.getHost(), address.getPort());
			context.getExpectedConfirmation().put(destinationAddress, message);
		}));
	}
	
	private Message buildPutMessage(AppContext context,
	                                String key,
	                                String value,
	                                long changeId,
	                                boolean isMulticast,
	                                Address destinationAddress) {
		
		Address srcAddress = context.getSrcAddress();
		
		Message message = new Message();
		
		message.setEventType(EventType.PUT);
		message.setKey(key);
		message.setSerializedValue(value);
		message.setChangeId(changeId);
		message.setSrcHost(srcAddress.getHost());
		message.setSrcPort(srcAddress.getPort());
		message.setMulticast(isMulticast);
		message.setDestinationHost(destinationAddress.getHost());
		message.setDestinationPort(destinationAddress.getPort());
		
		return message;
	}
	
	private Message buildConfirmationMessage(AppContext context, Event event) {
		
		Map<String, StoreValue> store = context.getStore();
		Address srcAddress = context.getSrcAddress();
		
		Message confirmationMessage = new Message();
		
		String key = event.getKey();
		
		Long changeId = key == null && store.containsKey(key) ? store.get(key).getChangeId() : event.getChangeId();
		
		RequestContext requestContext = new RequestContext();
		requestContext.setEventType(event.getEventType());
		requestContext.setSrcHost(event.getFromHost());
		requestContext.setSrcPort(event.getFromPort());
		requestContext.setChangeId(changeId);
		
		confirmationMessage.setEventType(EventType.CONFIRMATION);
		confirmationMessage.setDestinationHost(event.getFromHost());
		confirmationMessage.setDestinationPort(event.getFromPort());
		confirmationMessage.setSrcHost(srcAddress.getHost());
		confirmationMessage.setSrcPort(srcAddress.getPort());
		confirmationMessage.setRequestContext(requestContext);
		confirmationMessage.setChangeId(changeId);
		
		return confirmationMessage;
	}
}
