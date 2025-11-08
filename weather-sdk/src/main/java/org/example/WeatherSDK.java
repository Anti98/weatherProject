package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import org.example.weather.exceptions.ApiRequestException;
import org.example.weather.exceptions.CityNotFoundException;
import org.example.weather.exceptions.ConfigurationException;
import org.example.weather.exceptions.DataParseException;
import org.example.weather.exceptions.WeatherSDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WeatherSDK {
    private static final Logger log = LoggerFactory.getLogger(WeatherSDK.class);

    private final String apiKey;
    private final String baseUrl;
    private final Mode mode;
    private final WeatherCache cache;
    private final long ttlMillis;
    private final long pollingIntervalMillis;
    private ScheduledExecutorService scheduler;

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final WeatherApiClient apiClient = new WeatherApiClient(mapper);

    public WeatherSDK(String apiKey,
                      String baseUrl,
                      Mode mode,
                      int cacheSize,
                      long ttlMillis,
                      long pollingIntervalMillis) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new ConfigurationException("API key cannot be null or empty");
        }

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new ConfigurationException("Base URL cannot be null or empty");
        }
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.mode = mode;
        this.cache = new WeatherCache(cacheSize);
        this.ttlMillis = ttlMillis;
        this.pollingIntervalMillis = pollingIntervalMillis;

    }


    public WeatherResponse getWeather(String city) throws WeatherSDKException {
        log.debug("Requesting weather for city: {}", city);
        WeatherData cached = cache.get(city);

        try {
            if (mode == Mode.ON_DEMAND) {
                if (cached != null && !cached.isExpired(ttlMillis)) {
                    log.debug("Returning cached response for city: {}", city);
                    return cached.getResponse();
                }
                WeatherResponse freshWeatherResponse = fetchWeather(city);
                cache.put(city, new WeatherData(System.currentTimeMillis(), freshWeatherResponse));
                return freshWeatherResponse;
            } else {
                if (cached != null) {
                    log.debug("Returning pooling cache response for city: {}", city);
                    return cached.getResponse();
                }
                WeatherResponse fresh = fetchWeather(city);
                if (cache.getSize() == 0) {
                    startPolling();
                }
                cache.put(city, new WeatherData(System.currentTimeMillis(), fresh));
                return fresh;
            }
        } catch (WeatherSDKException e) {
            log.error("Failed to get weather for {}: {}", city, e.getMessage());
            throw e;
        }
    }

    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            log.debug("Polling scheduler stopped for API key: {}", apiKey);
        }
    }

    private WeatherResponse fetchWeather(String city) throws WeatherSDKException {
        log.debug("Fetching weather for city: {}", city);
        Coordinates coords = getCoordinates(city);
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(baseUrl))
                .newBuilder()
                .addQueryParameter("lat", String.valueOf(coords.getLat()))
                .addQueryParameter("lon", String.valueOf(coords.getLon()))
                .addQueryParameter("appid", apiKey)
                .addQueryParameter("units", "metric")
                .build();

        try {
            String json = apiClient.fetch(url.toString());
            WeatherResponse response = apiClient.parse(json, WeatherResponse.class);
            log.debug("Fetched weather for {} successfully", city);
            return response;
        } catch (ApiRequestException | DataParseException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("Unexpected error fetching weather for " + city, e);
        }
    }

    private void startPolling() {
        log.debug("Start polling with interval {}", pollingIntervalMillis);
        scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable pollingTask = () -> {
            log.debug("refreshing cache");
            for (String city : cache.getAll().keySet()) {
                try {
                    WeatherResponse response = fetchWeather(city);
                    cache.put(city, new WeatherData(System.currentTimeMillis(), response));
                } catch (WeatherSDKException e) {
                    log.error("Polling failed for {}: {}", city, e.getMessage());
                }
            }
        };

        scheduler.scheduleAtFixedRate(
                pollingTask,
                0,
                pollingIntervalMillis,
                TimeUnit.MILLISECONDS
        );
    }

    private Coordinates getCoordinates(String city) throws WeatherSDKException {
        log.debug("Getting coordinates for city: {}", city);

        Coordinates cached = SDKRegistry.getCoordinates(city);
        if (cached != null) return cached;

        try {
            String geoUrl = String.format(
                    "https://api.openweathermap.org/geo/1.0/direct?q=%s&limit=1&appid=%s",
                    city, apiKey
            );
            URL url = new URL(geoUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int status = conn.getResponseCode();
            if (status != 200) {
                throw new ApiRequestException("Failed to fetch coordinates. HTTP code: " + status);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                Coordinates[] list = mapper.readValue(reader, Coordinates[].class);
                if (list.length == 0) throw new CityNotFoundException(city);

                SDKRegistry.saveCoordinates(city, list[0]);
                return list[0];
            } catch (CityNotFoundException e) {
                throw e;
            } catch (Exception e) {
                throw new DataParseException("Failed to parse coordinates response for city: " + city, e);
            } finally {
                conn.disconnect();
            }
        } catch (WeatherSDKException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("Unexpected error while fetching coordinates for city: " + city, e);
        }
    }
}