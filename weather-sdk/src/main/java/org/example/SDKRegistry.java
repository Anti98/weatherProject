package org.example;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SDKRegistry {
    private static final Map<String, WeatherSDK> registry = new ConcurrentHashMap<>();
    private static final Map<String, Coordinates> cityCoordinates = new ConcurrentHashMap<>();

    public static synchronized WeatherSDK create(String apiKey,
                                                 String baseUrl,
                                                 Mode mode,
                                                 int cacheSize,
                                                 long ttlMillis,
                                                 long pollingIntervalMillis) {
        if (registry.containsKey(apiKey)) {
            throw new IllegalStateException("SDK with this key already exists!");
        }

        WeatherSDK sdk = new WeatherSDK(apiKey, baseUrl, mode, cacheSize, ttlMillis, pollingIntervalMillis);
        registry.put(apiKey, sdk);
        return sdk;
    }

    public static WeatherSDK get(String apiKey) {
        return registry.get(apiKey);
    }

    public static void unregister(String apiKey) {
        WeatherSDK sdk = registry.remove(apiKey);
        if (sdk != null) sdk.shutdown();
    }

    public static int size() {
        return registry.size();
    }

    public static Coordinates getCoordinates(String city) {
        return cityCoordinates.get(city.toLowerCase());
    }

    public static void saveCoordinates(String city, Coordinates coords) {
        cityCoordinates.put(city.toLowerCase(), coords);
    }
}