package ru.nsu.fit.d.cache.channel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
	
	private boolean isOk;
	
	private String text;
}
