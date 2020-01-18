package ru.nsu.fit.d.cache.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageType {
	
	GET("get"),
	
	PUT("put"),
	
	/**
	 * Сообщает новой ноде, чтобы та подписалась на мультикаст
	 */
	SUBSCRIBE("subscribe"),
	
	CONFIRMATION("confirmation");
	
	private String name;
}
