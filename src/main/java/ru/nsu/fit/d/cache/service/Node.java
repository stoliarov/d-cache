package ru.nsu.fit.d.cache.service;

import lombok.Getter;
import lombok.Setter;
import ru.nsu.fit.d.cache.channel.Receiver;
import ru.nsu.fit.d.cache.channel.Sender;
import ru.nsu.fit.d.cache.console.ConsoleReader;
import ru.nsu.fit.d.cache.event.EventQueue;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Node {
	
	private boolean isWriter;

	private Sender sender;

	private Receiver receiver;

	private EventQueue eventQueue;

	private Map<String, Integer> store;

	private ConsoleReader consoleReader;
	
	public Node() {
		
		EventQueue eventQueue = new EventQueue();
		
		this.eventQueue = eventQueue;
//		this.sender = new Sender();
		this.receiver = new Receiver(eventQueue);
		this.store = new HashMap<String, Integer>();
		this.consoleReader = new ConsoleReader(eventQueue);
	}
	
	public void run() {
		
		
		// TODO: 18.01.20 Запустить в отдельном потоке receiver, чтобы слушал входящие запросы
		
		// TODO: 18.01.20 вот тут выгребаем события из EventQueue и в зависимости от события реагируем,
		//  шлем сообщения с помощью Sender, меняем данные в store и т.д.
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
