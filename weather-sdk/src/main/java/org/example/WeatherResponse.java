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
}