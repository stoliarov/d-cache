package ru.nsu.fit.d.cache.app.handler;

import ru.nsu.fit.d.cache.app.model.Address;
import ru.nsu.fit.d.cache.app.model.AppContext;
import ru.nsu.fit.d.cache.app.model.StoreValue;
import ru.nsu.fit.d.cache.app.util.SenderService;
import ru.nsu.fit.d.cache.channel.Message;
import ru.nsu.fit.d.cache.channel.RequestContext;
import ru.nsu.fit.d.cache.queue.event.Event;
import ru.nsu.fit.d.cache.queue.event.EventType;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class ConfirmationHandler extends EventHandler {
	
	private SenderService senderService;
	
	public ConfirmationHandler() {
		this.senderService = new SenderService();
	}
	
	@Override
	public void handle(AppContext context, Event event) {
		
		EventType contextEventType = Optional.of(event)
				.map(Event::getRequestContext)
				.map(RequestContext::getEventType)
				.orElse(null);
		
		Address otherNodeAddress = new Address(event.getFromHost(), event.getFromPort());
		
		context.getExpectedConfirmation().remove(otherNodeAddress);
		
		boolean isWriter = context.isWriter();
		
		if (isWriter && contextEventType == EventType.SUBSCRIBE) {
			
			startStoreSharing(context, otherNodeAddress);
			sendNextValueFromStore(context, otherNodeAddress);
			
		} else if (isWriter && contextEventType == EventType.STORE_SHARING) {
			
			sendNextValueFromStore(context, otherNodeAddress);
		}
		
		// TODO: 18.01.20 В зависимости от контекста.
		//  Например, в случае подтверждения подписки начать обход стора и его отправку
	}
	
	/**
	 * Инициирует рассылку всех данных из стора.
	 *
	 * @param destinationAddress получатель рассылки
	 */
	private void startStoreSharing(AppContext context, Address destinationAddress) {
		
		Iterator<Map.Entry<String, StoreValue>> iterator = context.getStore().entrySet().iterator();
		
		Address otherNodeAddress = new Address(destinationAddress.getHost(), destinationAddress.getPort());
		
		context.getActiveStoreIterators().put(otherNodeAddress, iterator);
	}
	
	/**
	 * Использует writer.
	 * Отправляет следующее сообщение для данного ридера в рамках рассылки стора при инициализации новой ноды
	 *
	 * @param destinationAddress адрес ридера, для которого производится рассылка
	 */
	private void sendNextValueFromStore(AppContext context, Address destinationAddress) {
		
		String destinationHost = destinationAddress.getHost();
		int destinationPort = destinationAddress.getPort();
		
		Address srcAddress = context.getSrcAddress();
		
		Iterator<Map.Entry<String, StoreValue>> iterator = context.getActiveStoreIterators().get(destinationAddress);
		
		if (!iterator.hasNext()) {
			context.getActiveStoreIterators().remove(destinationAddress);
			
			Message message = Message.builder()
					.eventType(EventType.END_OF_STORE)
					.srcHost(srcAddress.getHost())
					.srcPort(srcAddress.getPort())
					.destinationHost(destinationHost)
					.destinationPort(destinationPort)
					.build();
			
			senderService.sendMessage(context, message);

			return;
		}
		
		Map.Entry<String, StoreValue> storeEntry = iterator.next();
		StoreValue storeValue = storeEntry.getValue();
		
		Message message = Message.builder()
				.key(storeEntry.getKey())
				.serializedValue(storeValue.getSerializedValue())
				.changeId(storeValue.getChangeId())
				.eventType(EventType.STORE_SHARING)
				.isLowPriorityValue(true)
				.srcHost(srcAddress.getHost())
				.srcPort(srcAddress.getPort())
				.destinationHost(destinationHost)
				.destinationPort(destinationPort)
				.build();

		System.out.println("confirmation handler");

		senderService.sendMessage(context, message);
	}
}
