package com.agriconnect.marketplace.dto;

import com.agriconnect.marketplace.entity.Listing;

import java.math.BigDecimal;

public class ListingResponse {
    private Long id;
    private Long organizationId;
    private Long farmId;
    private String produceName;
    private String description;
    private BigDecimal pricePerUnit;
    private String unit;
    private BigDecimal quantityAvailable;
    private String status;

    public static ListingResponse from(Listing listing) {
        ListingResponse r = new ListingResponse();
        r.id = listing.getId();
        r.organizationId = listing.getOrganizationId();
        r.farmId = listing.getFarmId();
        r.produceName = listing.getProduceName();
        r.description = listing.getDescription();
        r.pricePerUnit = listing.getPricePerUnit();
        r.unit = listing.getUnit();
        r.quantityAvailable = listing.getQuantityAvailable();
        r.status = listing.getStatus().name();
        return r;
    }

    public Long getId() { return id; }
    public Long getOrganizationId() { return organizationId; }
    public Long getFarmId() { return farmId; }
    public String getProduceName() { return produceName; }
    public String getDescription() { return description; }
    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public String getUnit() { return unit; }
    public BigDecimal getQuantityAvailable() { return quantityAvailable; }
    public String getStatus() { return status; }
}
