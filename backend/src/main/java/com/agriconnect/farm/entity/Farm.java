package com.agriconnect.farm.entity;

import com.agriconnect.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "farms")
public class Farm extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    @Column(name = "size_hectares")
    private Double sizeHectares;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getSizeHectares() { return sizeHectares; }
    public void setSizeHectares(Double sizeHectares) { this.sizeHectares = sizeHectares; }
}
