package ru.nsu.fit.d.cache.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreValue {
	
	private String serializedValue;
	
	private Long changeId;
}
