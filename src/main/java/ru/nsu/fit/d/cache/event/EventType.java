package ru.nsu.fit.d.cache.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventType {
	
	/**
	 * Запрос на запись в локальный store
	 */
	WRITE_TO_STORE("write_to_store"),
	
	/**
	 * К сети подключилась новая нода
	 */
	NEW_CONNECTION("new_connection"),
	
	/**
	 * Получено подтверждение записи в локальный store от другой ноды
	 */
	CONFIRMATION("confirmation");
	
	private String name;
}
