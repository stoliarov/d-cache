package ru.nsu.fit.d.cache.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RequestType {
	
	GET("GET"),
	
	PUT("PUT");
	
	private String name;
}
