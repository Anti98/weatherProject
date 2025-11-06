package org.example.weather.exceptions;

public class DataParseException extends WeatherSDKException {
    public DataParseException(String message, Throwable cause) {
        super(message, cause);
    }
}