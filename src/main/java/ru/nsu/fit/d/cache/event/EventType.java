package ru.nsu.fit.d.cache.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventType {
	
	/**
	 * Получен запрос
	 */
	REQUEST("request"),
	
	/**
	 * Получен ответ
	 */
	RESPONSE("response");
	
	private String name;
}
