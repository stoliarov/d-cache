package ru.nsu.fit.d.cache.channel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message<T> {
	
	private MessageType messageType;
	
	private String key;
	
	private T value;
	
	private Long changeId;
	
	private String freeText;
	
	private boolean isLowPriorityValue;
	
	private String srcUrl;
	
	/**
	 * Другие ноды при отправке подтверждения указывают какой запрос они подтвердили
	 */
	private Message responseContext;
}
