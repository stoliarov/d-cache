package ru.nsu.fit.d.cache.queue.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EventType {

	/**
	 * Неизвестный тип события
	 */
	UNKNOWN("unknown"),

	/**
	 * Запрос на запись в локальный store
	 */
	PUT("put"),
	
	/**
	 * К сети подключилась новая нода
	 */
	NEW_CONNECTION("new_connection"),
	
	/**
	 * Получено подтверждение записи в локальный store от другой ноды
	 */
	CONFIRMATION("confirmation"),

	/**
	 * Получен адрес мультикаста, нужно на него подписаться
	 */
	SUBSCRIBE("subscribe"),
	
	/**
	 * Одно из сообщений рассылки стора при инициализации новой ноды
	 */
	STORE_SHARING("store_sharing"),
	
	/**
	 * Сигнал ридеру о том, что все сообщения из стора отправлены - дальше можно обновляться только по мультикасту.
	 */
	END_OF_STORE("end_of_store"),

	/**
	 * Данные c консоли
	 */
	FROM_CONSOLE("from_console");

	private String name;
}
