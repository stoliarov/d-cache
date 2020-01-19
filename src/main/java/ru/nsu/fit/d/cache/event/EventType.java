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
	RESPONSE("response"),

	/**
	 * Получены данные с консоли
	 */
	CONSOLE("console");

	private String name;
}
