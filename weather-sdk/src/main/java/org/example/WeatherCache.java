package org.example;

import java.util.LinkedHashMap;
import java.util.Map;

public class WeatherCache {
    private final int maxSize;
    private final Map<String, WeatherData> cache;

    public WeatherCache(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new LinkedHashMap<>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, WeatherData> eldest) {
                return size() > WeatherCache.this.maxSize;
            }
        };
    }

    public synchronized WeatherData get(String city) {
        return cache.get(city.toLowerCase());
    }

    public synchronized void put(String city, WeatherData data) {
        cache.put(city.toLowerCase(), data);
    }

    public synchronized Map<String, WeatherData> getAll() {
        return new LinkedHashMap<>(cache);
    }
}