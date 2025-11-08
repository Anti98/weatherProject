# Weather SDK

## Overview
Weather SDK is a lightweight Java library designed to interact with the **OpenWeatherMap API**.  
It provides an easy and flexible way to retrieve weather data for specific cities, manage caching, and handle both **on-demand** and **polling** data fetching modes.

The project consists of **two modules**:
- **weather-sdk** — the main library (SDK) that contains the core logic.
- **weather-demo** — a demonstration module showing how to use the SDK in a simple example.

---

## Architecture

### 1. SDKRegistry
`SDKRegistry` is the central manager responsible for creating and managing SDK instances.  
It contains:
- **SDK cache** — keeps all created SDK instances.
- **City coordinates cache** — stores latitude and longitude for each requested city to avoid repeated API calls.

### 2. WeatherSDK
Each SDK instance manages its own local cache and provides two main operations:
- `getCoordinates(String city)` — fetches coordinates (latitude, longitude) for a given city.
- `getWeather(String city)` — retrieves weather data for the specified city using cached coordinates if available.

### 3. Modes of operation
- **ON_DEMAND** — makes a request each time data is needed.
- **POLLING** — periodically updates weather data in the background based on a configured interval.

---

## Error Handling

Custom exceptions are used to handle different types of errors clearly and safely:

| Exception | Description |
|------------|--------------|
| `WeatherSDKException` | Base class for all SDK-related exceptions. |
| `ApiRequestException` | Thrown when API communication fails (network or HTTP errors). |
| `CityNotFoundException` | Raised when the specified city cannot be found via the geocoding API. |
| `ConfigurationException` | Indicates misconfiguration in the SDK setup. |
| `DataParseException` | Thrown when API response parsing fails (e.g., JSON errors). |

All exceptions extend from `WeatherSDKException`, which in turn extends `RuntimeException`.

---
## Example Usage

```java
// ON_DEMAND mode example
WeatherSDK sdkOnDemand = SDKRegistry.create(
        "YOUR_API_KEY",
        "https://api.openweathermap.org/data/2.5/weather",
        Mode.ON_DEMAND,
        5,                  // cache size
        10 * 60 * 1000,     // TTL: 10 minutes
        0                   // polling interval not used in ON_DEMAND
);

// POLLING mode example
WeatherSDK sdkPolling = SDKRegistry.create(
        "YOUR_API_KEY_2",
        "https://api.openweathermap.org/data/2.5/weather",
        Mode.POLLING,
        5,                  // cache size
        0,                  // TTL not used in POLLING
        60 * 1000           // Polling interval: 1 minute
);
---

## Notes
- The SDK internally uses `ObjectMapper` (Jackson) for JSON deserialization.
- The SDK supports caching to reduce redundant API calls.
- Each SDK instance can be configured independently.

---

## Possible Improvements
- Merge caches between SDK instances to avoid duplicated data requests.
- Implement batch requests to update multiple cities at once instead of one by one.
- Store cached data for cities in multiple languages for localization support.