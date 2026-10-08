package com.agriconnect.farm.dto;

import com.agriconnect.farm.entity.Livestock;

public class LivestockResponse {
    private Long id;
    private Long farmId;
    private String species;
    private String breed;
    private Integer quantity;
    private String healthNotes;

    public static LivestockResponse from(Livestock livestock) {
        LivestockResponse response = new LivestockResponse();
        response.id = livestock.getId();
        response.farmId = livestock.getFarm().getId();
        response.species = livestock.getSpecies();
        response.breed = livestock.getBreed();
        response.quantity = livestock.getQuantity();
        response.healthNotes = livestock.getHealthNotes();
        return response;
    }

    public Long getId() { return id; }
    public Long getFarmId() { return farmId; }
    public String getSpecies() { return species; }
    public String getBreed() { return breed; }
    public Integer getQuantity() { return quantity; }
    public String getHealthNotes() { return healthNotes; }
}
