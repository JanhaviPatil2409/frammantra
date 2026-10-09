package com.farmmantra.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.farmmantra.dto.AdvisoryDto;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class WeatherService {

    private static final String GEO_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast";
    private static final String DEFAULT_LOCATION = "Nashik";
    private static final double DEFAULT_LAT = 19.9975;
    private static final double DEFAULT_LON = 73.7898;
    private static final Duration FORECAST_TTL = Duration.ofMinutes(30);

    private final RestClient http = RestClient.create();
    private final Map<String, Coordinates> geoCache = new ConcurrentHashMap<>();
    private final Map<String, CachedForecast> forecastCache = new ConcurrentHashMap<>();

    public record Coordinates(double lat, double lon, String resolvedName) {}

    private record CachedForecast(AdvisoryDto.Weather weather, Instant fetchedAt) {}

    /** Turns a district name such as "Nashik" into coordinates. Cached in memory. */
    public Coordinates geocode(String location) {
        String query = (location == null || location.isBlank()) ? DEFAULT_LOCATION : location.trim();
        return geoCache.computeIfAbsent(query.toLowerCase(), k -> lookup(query));
    }

    private Coordinates lookup(String query) {
        try {
            JsonNode root = http.get()
                    .uri(GEO_URL + "?name={name}&count=1&countryCode=IN&format=json", query)
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode first = (root == null) ? null : root.path("results").path(0);
            if (first == null || first.isMissingNode()) {
                return new Coordinates(DEFAULT_LAT, DEFAULT_LON, DEFAULT_LOCATION);
            }
            return new Coordinates(
                    first.path("latitude").asDouble(),
                    first.path("longitude").asDouble(),
                    first.path("name").asText(query));
        } catch (RuntimeException e) {
            // If geocoding fails, fall back to the default location instead of failing the page
            return new Coordinates(DEFAULT_LAT, DEFAULT_LON, DEFAULT_LOCATION);
        }
    }

    /** Returns the forecast, reusing results for 30 minutes to limit API calls. */
    public AdvisoryDto.Weather getForecast(Coordinates c) {
        String key = c.lat() + "," + c.lon();
        CachedForecast cached = forecastCache.get(key);

        if (cached != null && cached.fetchedAt().plus(FORECAST_TTL).isAfter(Instant.now())) {
            return cached.weather();
        }

        try {
            AdvisoryDto.Weather fresh = fetchForecast(c);
            forecastCache.put(key, new CachedForecast(fresh, Instant.now()));
            return fresh;
        } catch (RuntimeException e) {
            // Rate limited or offline: serve the last good data if we have it
            if (cached != null) {
                return cached.weather();
            }
            throw e;
        }
    }

    private AdvisoryDto.Weather fetchForecast(Coordinates c) {
        JsonNode root = http.get()
                .uri(FORECAST_URL + "?latitude={lat}&longitude={lon}"
                        + "&current=temperature_2m,relative_humidity_2m,wind_speed_10m"
                        + "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,"
                        + "precipitation_probability_max,wind_speed_10m_max"
                        + "&timezone=Asia/Kolkata&forecast_days=7",
                        c.lat(), c.lon())
                .retrieve()
                .body(JsonNode.class);

        if (root == null) {
            throw new IllegalStateException("Empty response from weather service");
        }

        JsonNode cur = root.path("current");
        AdvisoryDto.Current current = new AdvisoryDto.Current(
                cur.path("temperature_2m").asDouble(),
                cur.path("relative_humidity_2m").asInt(),
                cur.path("wind_speed_10m").asDouble());

        JsonNode d = root.path("daily");
        List<AdvisoryDto.Day> days = new ArrayList<>();
        for (int i = 0; i < d.path("time").size(); i++) {
            days.add(new AdvisoryDto.Day(
                    d.path("time").get(i).asText(),
                    d.path("temperature_2m_max").get(i).asDouble(),
                    d.path("temperature_2m_min").get(i).asDouble(),
                    d.path("precipitation_sum").get(i).asDouble(),
                    d.path("precipitation_probability_max").get(i).asInt(),
                    d.path("wind_speed_10m_max").get(i).asDouble()));
        }

        return new AdvisoryDto.Weather(current, days);
    }
}