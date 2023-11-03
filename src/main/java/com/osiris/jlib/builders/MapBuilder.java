package com.osiris.jlib.builders;

import java.util.HashMap;
import java.util.Map;

public class MapBuilder<K, V> {
    public Map<K, V> map = new HashMap<>();

    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    public MapBuilder<K, V> remove(K key) {
        map.remove(key);
        return this;
    }
}
