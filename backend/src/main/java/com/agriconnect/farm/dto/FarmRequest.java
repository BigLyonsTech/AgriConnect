package com.agriconnect.farm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class FarmRequest {

    @NotBlank(message = "Farm name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Size in hectares is required")
    @Positive(message = "Size in hectares must be positive")
    private Double sizeHectares;

    public FarmRequest() {}

    public FarmRequest(String name, String location, Double sizeHectares) {
        this.name = name;
        this.location = location;
        this.sizeHectares = sizeHectares;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getSizeHectares() { return sizeHectares; }
    public void setSizeHectares(Double sizeHectares) { this.sizeHectares = sizeHectares; }
}
