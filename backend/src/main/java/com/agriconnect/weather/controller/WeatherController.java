package com.agriconnect.weather.controller;

import com.agriconnect.farm.entity.Farm;
import com.agriconnect.farm.service.FarmService;
import com.agriconnect.weather.client.WeatherClient;
import com.agriconnect.weather.dto.WeatherResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/farms")
public class WeatherController {

    private final FarmService farmService;
    private final WeatherClient weatherClient;

    public WeatherController(FarmService farmService, WeatherClient weatherClient) {
        this.farmService = farmService;
        this.weatherClient = weatherClient;
    }

    @GetMapping("/{id}/weather")
    public WeatherResponse getWeatherForFarm(@PathVariable Long id) {
        Farm farm = farmService.getOwnedFarm(id);
        return weatherClient.fetchByCity(farm.getLocation());
    }
}
