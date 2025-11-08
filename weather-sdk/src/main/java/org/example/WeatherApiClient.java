package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.example.weather.exceptions.ApiRequestException;
import org.example.weather.exceptions.DataParseException;

import java.io.IOException;
import java.time.Duration;

public class WeatherApiClient {
    private final OkHttpClient client;
    private final ObjectMapper mapper;

    public WeatherApiClient(ObjectMapper mapper) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = mapper;
    }

    public String fetch(String url) throws ApiRequestException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String body = response.body() != null ? response.body().string() : "";
                throw new ApiRequestException("HTTP " + response.code() + " - " + body);
            }
            return response.body() != null ? response.body().string() : "";
        } catch (IOException e) {
            throw new ApiRequestException("Network error while calling API: " + url, e);
        }
    }

    public <T> T parse(String json, Class<T> type) throws DataParseException {
        try {
            return mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new DataParseException("Failed to parse JSON", e);
        }
    }

}