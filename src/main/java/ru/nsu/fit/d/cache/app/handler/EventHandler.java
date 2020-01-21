package ru.nsu.fit.d.cache.app.handler;

import ru.nsu.fit.d.cache.app.model.AppContext;
import ru.nsu.fit.d.cache.queue.event.Event;

public abstract class EventHandler {
	
	public void handle(AppContext context, Event event) {}
}
