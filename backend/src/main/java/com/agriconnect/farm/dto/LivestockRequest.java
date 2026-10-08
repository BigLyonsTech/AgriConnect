package com.agriconnect.farm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class LivestockRequest {

    @NotNull(message = "Farm id is required")
    private Long farmId;

    @NotBlank(message = "Species is required")
    private String species;

    private String breed;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    private String healthNotes;

    public Long getFarmId() { return farmId; }
    public void setFarmId(Long farmId) { this.farmId = farmId; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getHealthNotes() { return healthNotes; }
    public void setHealthNotes(String healthNotes) { this.healthNotes = healthNotes; }
}
