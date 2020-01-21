package ru.nsu.fit.d.cache.app.handler;

import ru.nsu.fit.d.cache.app.model.AppContext;
import ru.nsu.fit.d.cache.app.util.SenderService;
import ru.nsu.fit.d.cache.queue.event.Event;

public class EndOfStoreHandler extends EventHandler {
	
	private SenderService senderService;
	
	public EndOfStoreHandler() {
		this.senderService = new SenderService();
	}
	
	@Override
	public void handle(AppContext context, Event event) {
		
		context.setInitCompleted(true);
		
		senderService.sendConfirmation(context, event);
	}
}
