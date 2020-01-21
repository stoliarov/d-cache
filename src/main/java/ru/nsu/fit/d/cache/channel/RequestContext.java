package ru.nsu.fit.d.cache.channel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.nsu.fit.d.cache.queue.event.EventType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestContext {
	
	private EventType messageType;
	
	private Long changeId;
	
	private String multicastHost;

	private int multicastPort;
	
	private String srcHost;
	
	private int srcPort;
}
