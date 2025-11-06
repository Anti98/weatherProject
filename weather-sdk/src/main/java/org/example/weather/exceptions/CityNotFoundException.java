package org.example.weather.exceptions;

public class CityNotFoundException extends WeatherSDKException {
    public CityNotFoundException(String city) {
        super("City not found: " + city);
    }
}