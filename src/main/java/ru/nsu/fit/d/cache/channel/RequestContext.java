package ru.nsu.fit.d.cache.channel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestContext {
	
	private MessageType messageType;
	
	private Long changeId;
	
	private String freeText;
	
	private String srcUrl;
}
