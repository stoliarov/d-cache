package ru.nsu.fit.d.cache.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageType {

	/**
	 * Неизвестный тип сообщения
	 */
	UNKNOWN("unknown"),

	/**
	 * Запрос на существующие данные, при подключении новой ноды
	 */
	GET("get"),

	/**
	 * Запрос на сохранение сообщения к кэше
	 */
	PUT("put"),
	
	/**
	 * Сообщает новой ноде, чтобы та подписалась на мультикаст
	 */
	SUBSCRIBE("subscribe"),

	/**
	 * Подтверждение получения сообщения
	 */
	CONFIRMATION("confirmation");
	
	private String name;
}
