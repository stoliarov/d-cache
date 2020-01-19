package ru.nsu.fit.d.cache.console;

import org.apache.commons.lang3.Validate;

public class Data {
    private String key;
    private String value;

    public Data(String key, String value) {
        Validate.notNull(key, "key cannot be null");
        Validate.notNull(value, "value cannot be null");

        this.key = key;
        this.value = value;
    }
}
