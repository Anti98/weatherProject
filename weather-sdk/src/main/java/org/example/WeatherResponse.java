package org.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherResponse {
    public Weather weather[];
    public Main main;
    public Wind wind;
    public Sys sys;
    public long dt;
    public int timezone;
    public String name;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Weather {
        public String main;
        public String description;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Main {
        public double temp;
        public double feels_like;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Wind {
        public double speed;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sys {
        public long sunrise;
        public long sunset;
    }
    public String getWeatherInfo() {
        String cityName = name != null ? name : "Unknown city";
        String weatherDescription = "No weather data";
        String temperatureInfo = "No temperature data";

        if (weather != null && weather.length > 0 && weather[0] != null) {
            weatherDescription = weather[0].main + " (" + weather[0].description + ")";
        }

        if (main != null) {
            temperatureInfo = String.format("%.1f°C, feels like %.1f°C", main.temp, main.feels_like);
        }

        return String.format("City: %s, Weather: %s, Temperature: %s", cityName, weatherDescription, temperatureInfo);
    }
}