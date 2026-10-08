package com.agriconnect.farm.dto;

import com.agriconnect.farm.entity.Crop;

import java.time.LocalDate;

public class CropResponse {
    private Long id;
    private Long farmId;
    private String farmName;
    private String cropName;
    private LocalDate plantingDate;
    private LocalDate expectedHarvestDate;
    private String status;

    public static CropResponse from(Crop crop) {
        CropResponse response = new CropResponse();
        response.id = crop.getId();
        response.farmId = crop.getFarm().getId();
        response.farmName = crop.getFarm().getName();
        response.cropName = crop.getCropName();
        response.plantingDate = crop.getPlantingDate();
        response.expectedHarvestDate = crop.getExpectedHarvestDate();
        response.status = crop.getStatus().name();
        return response;
    }

    public Long getId() { return id; }
    public Long getFarmId() { return farmId; }
    public String getFarmName() { return farmName; }
    public String getCropName() { return cropName; }
    public LocalDate getPlantingDate() { return plantingDate; }
    public LocalDate getExpectedHarvestDate() { return expectedHarvestDate; }
    public String getStatus() { return status; }
}
