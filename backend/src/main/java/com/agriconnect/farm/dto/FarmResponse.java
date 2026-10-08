package com.agriconnect.farm.dto;

import com.agriconnect.farm.entity.Farm;

import java.time.Instant;

public class FarmResponse {
    private Long id;
    private String name;
    private String location;
    private Double sizeHectares;
    private Instant createdAt;

    public static FarmResponse from(Farm farm) {
        FarmResponse response = new FarmResponse();
        response.id = farm.getId();
        response.name = farm.getName();
        response.location = farm.getLocation();
        response.sizeHectares = farm.getSizeHectares();
        response.createdAt = farm.getCreatedAt();
        return response;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public Double getSizeHectares() { return sizeHectares; }
    public Instant getCreatedAt() { return createdAt; }
}
