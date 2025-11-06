package org.example.weather.exceptions;

public class ConfigurationException extends WeatherSDKException {
    public ConfigurationException(String message) {
        super(message);
    }
}