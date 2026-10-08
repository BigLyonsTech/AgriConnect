package com.agriconnect.farm.entity;

import com.agriconnect.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "crops")
public class Crop extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @Column(name = "crop_name", nullable = false)
    private String cropName;

    @Column(name = "planting_date")
    private LocalDate plantingDate;

    @Column(name = "expected_harvest_date")
    private LocalDate expectedHarvestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CropStatus status;

    public Farm getFarm() { return farm; }
    public void setFarm(Farm farm) { this.farm = farm; }

    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }

    public LocalDate getPlantingDate() { return plantingDate; }
    public void setPlantingDate(LocalDate plantingDate) { this.plantingDate = plantingDate; }

    public LocalDate getExpectedHarvestDate() { return expectedHarvestDate; }
    public void setExpectedHarvestDate(LocalDate expectedHarvestDate) { this.expectedHarvestDate = expectedHarvestDate; }

    public CropStatus getStatus() { return status; }
    public void setStatus(CropStatus status) { this.status = status; }
}
