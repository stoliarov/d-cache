package ru.nsu.fit.d.cache.queue.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.nsu.fit.d.cache.channel.RequestContext;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event<T> {
	
	private EventType eventType;
	
	private String key;
	
	private T value;
	
	private long changeId;
	
	private boolean isLowPriorityValue;
	
	private String fromHost;
	
	private int fromPort;
	
	private RequestContext requestContext;
}
