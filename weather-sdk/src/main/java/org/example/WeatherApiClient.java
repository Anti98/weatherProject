package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherApiClient {
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static WeatherResponse fetch(String apiKey, String city) throws Exception {
        String url = BASE_URL + "?q=" + city + "&appid=" + apiKey + "&units=metric";
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed request: " + response.code() + " - " + response.message());
            }

            String body = response.body().string();
            return mapper.readValue(body, WeatherResponse.class);
        }
    }
}