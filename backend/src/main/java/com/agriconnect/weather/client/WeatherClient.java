package com.agriconnect.weather.client;

import com.agriconnect.weather.dto.WeatherResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Wraps OpenWeatherMap's current-weather endpoint. Needs a real API key
 * (app.weather.api-key / WEATHER_API_KEY env var) and internet access -
 * neither available in the sandbox this was written in, so this compiles
 * and is structurally correct but wasn't exercised against the live API.
 * Get a free key at openweathermap.org and test it locally.
 */
@Component
public class WeatherClient {

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    @Value("${app.weather.api-key:}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherResponse fetchByCity(String city) {
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String url = String.format("%s?q=%s&appid=%s&units=metric", BASE_URL, encodedCity, apiKey);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = objectMapper.readTree(response.body());

            if (json.has("cod") && json.path("cod").asInt() != 200) {
                throw new WeatherLookupException("Weather lookup failed for '" + city + "': " + json.path("message").asText());
            }

            double temp = json.path("main").path("temp").asDouble();
            double humidity = json.path("main").path("humidity").asDouble();
            double wind = json.path("wind").path("speed").asDouble();
            String description = json.path("weather").isArray() && json.path("weather").size() > 0
                    ? json.path("weather").get(0).path("description").asText()
                    : "unknown";

            return new WeatherResponse(city, temp, description, humidity, wind);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WeatherLookupException("Could not reach the weather service: " + e.getMessage());
        } catch (java.io.IOException e) {
            throw new WeatherLookupException("Could not reach the weather service: " + e.getMessage());
        }
    }

    public static class WeatherLookupException extends RuntimeException {
        public WeatherLookupException(String message) {
            super(message);
        }
    }
}
