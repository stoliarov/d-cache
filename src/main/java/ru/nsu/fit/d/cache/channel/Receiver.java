package ru.nsu.fit.d.cache.channel;

import lombok.AllArgsConstructor;
import ru.nsu.fit.d.cache.event.EventQueue;

@AllArgsConstructor
public class Receiver {
	
	private static final int WAIT_TIMEOUT = 200;
	
	private EventQueue eventQueue;
	
	private int port;
	
	public Message receive() {
		
		// TODO: 18.01.20 Запускается в отдельном потоке, блокируется и ждет запросы.
		//  Кладет полученное в очередь как event. Далее из этой очереди класс Node будет выгребать запросы и реагировать
		
		return null;
	}
}
