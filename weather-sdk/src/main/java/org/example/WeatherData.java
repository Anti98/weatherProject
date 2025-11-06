package org.example;

public class WeatherData {
    private long timestamp;
    private WeatherResponse response;

    public WeatherData(long timestamp, WeatherResponse response) {
        this.timestamp = timestamp;
        this.response = response;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public WeatherResponse getResponse() {
        return response;
    }

    public boolean isExpired(long ttlMillis) {
        return System.currentTimeMillis() - timestamp > ttlMillis;
    }
}