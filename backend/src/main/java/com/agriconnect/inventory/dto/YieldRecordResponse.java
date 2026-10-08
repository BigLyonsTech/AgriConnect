package com.agriconnect.inventory.dto;

import com.agriconnect.inventory.entity.YieldRecord;

import java.time.LocalDate;

public class YieldRecordResponse {
    private Long id;
    private Long cropId;
    private String cropName;
    private LocalDate harvestDate;
    private Double quantityKg;

    public static YieldRecordResponse from(YieldRecord record) {
        YieldRecordResponse r = new YieldRecordResponse();
        r.id = record.getId();
        r.cropId = record.getCrop().getId();
        r.cropName = record.getCrop().getCropName();
        r.harvestDate = record.getHarvestDate();
        r.quantityKg = record.getQuantityKg();
        return r;
    }

    public Long getId() { return id; }
    public Long getCropId() { return cropId; }
    public String getCropName() { return cropName; }
    public LocalDate getHarvestDate() { return harvestDate; }
    public Double getQuantityKg() { return quantityKg; }
}
