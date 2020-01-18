package ru.nsu.fit.d.cache.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.nsu.fit.d.cache.channel.Message;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event<T> {
	
	private EventType eventType;
	
	private String key;
	
	private T value;
	
	private long changeId;
	
	private boolean isLowPriorityValue;
	
	private String fromUrl;
	
	private Message responseContext;
}
