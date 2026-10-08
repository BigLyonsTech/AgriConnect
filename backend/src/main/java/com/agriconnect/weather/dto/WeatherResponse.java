package com.agriconnect.weather.dto;

public class WeatherResponse {
    private String location;
    private double temperatureCelsius;
    private String description;
    private double humidityPercent;
    private double windSpeedMs;

    public WeatherResponse(String location, double temperatureCelsius, String description,
                            double humidityPercent, double windSpeedMs) {
        this.location = location;
        this.temperatureCelsius = temperatureCelsius;
        this.description = description;
        this.humidityPercent = humidityPercent;
        this.windSpeedMs = windSpeedMs;
    }

    public String getLocation() { return location; }
    public double getTemperatureCelsius() { return temperatureCelsius; }
    public String getDescription() { return description; }
    public double getHumidityPercent() { return humidityPercent; }
    public double getWindSpeedMs() { return windSpeedMs; }
}
