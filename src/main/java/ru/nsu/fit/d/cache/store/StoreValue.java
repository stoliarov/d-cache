package ru.nsu.fit.d.cache.store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreValue<T> {
	
	private T value;
	
	private long changeId;
}
