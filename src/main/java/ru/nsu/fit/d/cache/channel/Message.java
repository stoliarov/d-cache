package ru.nsu.fit.d.cache.channel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Validate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
	
	private MessageType messageType;

	private String key;
	
	private String serializedValue;
	
	private Long changeId;

	private String multicastHost;

	private int multicastPort;
	
	private boolean isLowPriorityValue;
	
	private String destinationHost;

	private int destinationPort;

	private String srcHost;

	private int srcPort;

	private boolean isMulticast;

	/**
	 * Другие ноды при отправке подтверждения указывают какой запрос они подтвердили
	 */
	private RequestContext requestContext;
	
	private long sendingTime;
	
	private boolean isSent = false;
	
	public void send(long time) {
		setSendingTime(time);
		setSent(true);
	}

	public Message(String key, String serializedValue) {
		Validate.notNull(key, "type cannot be null");
		Validate.notNull(serializedValue, "data cannot be null");

		this.key = key;
		this.serializedValue = serializedValue;
	}
}
