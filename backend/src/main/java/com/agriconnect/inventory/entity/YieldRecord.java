package com.agriconnect.inventory.entity;

import com.agriconnect.common.base.BaseEntity;
import com.agriconnect.farm.entity.Crop;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "yield_records")
public class YieldRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    @Column(name = "harvest_date", nullable = false)
    private LocalDate harvestDate;

    @Column(name = "quantity_kg", nullable = false)
    private Double quantityKg;

    public Crop getCrop() { return crop; }
    public void setCrop(Crop crop) { this.crop = crop; }

    public LocalDate getHarvestDate() { return harvestDate; }
    public void setHarvestDate(LocalDate harvestDate) { this.harvestDate = harvestDate; }

    public Double getQuantityKg() { return quantityKg; }
    public void setQuantityKg(Double quantityKg) { this.quantityKg = quantityKg; }
}
