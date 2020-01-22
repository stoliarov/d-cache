package ru.nsu.fit.d.cache.app.handler;

import ru.nsu.fit.d.cache.app.model.AppContext;
import ru.nsu.fit.d.cache.app.model.StoreValue;
import ru.nsu.fit.d.cache.app.util.SenderService;
import ru.nsu.fit.d.cache.queue.event.Event;

import java.util.Map;

public class StoreSharingHandler extends EventHandler {
	
	private SenderService senderService;
	
	public StoreSharingHandler() {
		this.senderService = new SenderService();
	}
	
	@Override
	public void handle(AppContext context, Event event) {
		
		String key = event.getKey();
		
		Map<String, StoreValue> store = context.getStore();
		
		if (!store.containsKey(key)) {
			store.put(key, new StoreValue(event.getSerializedValue(), event.getChangeId()));
		}

		System.out.println("store sharing handler");

		context.getStore().forEach((k, v) -> System.out.println(v.getSerializedValue()));

		senderService.sendConfirmation(context, event);
	}
}
