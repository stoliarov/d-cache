package ru.nsu.fit.d.cache.queue.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.nsu.fit.d.cache.channel.RequestContext;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
	
	private EventType eventType;
	
	private String key;
	
	private String serializedValue;
	
	private long changeId;
	
	private boolean isLowPriorityValue;
	
	private String fromHost;
	
	private int fromPort;
	
	private RequestContext requestContext;
}
