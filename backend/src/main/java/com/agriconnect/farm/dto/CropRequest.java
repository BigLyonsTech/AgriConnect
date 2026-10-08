package com.agriconnect.farm.dto;

import com.agriconnect.farm.entity.CropStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class CropRequest {

    @NotNull(message = "Farm id is required")
    private Long farmId;

    @NotBlank(message = "Crop name is required")
    private String cropName;

    private LocalDate plantingDate;
    private LocalDate expectedHarvestDate;

    @NotNull(message = "Status is required")
    private CropStatus status;

    public Long getFarmId() { return farmId; }
    public void setFarmId(Long farmId) { this.farmId = farmId; }

    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }

    public LocalDate getPlantingDate() { return plantingDate; }
    public void setPlantingDate(LocalDate plantingDate) { this.plantingDate = plantingDate; }

    public LocalDate getExpectedHarvestDate() { return expectedHarvestDate; }
    public void setExpectedHarvestDate(LocalDate expectedHarvestDate) { this.expectedHarvestDate = expectedHarvestDate; }

    public CropStatus getStatus() { return status; }
    public void setStatus(CropStatus status) { this.status = status; }
}
