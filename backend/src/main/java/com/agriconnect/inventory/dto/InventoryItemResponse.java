package com.agriconnect.inventory.dto;

import com.agriconnect.inventory.entity.InventoryItem;

public class InventoryItemResponse {
    private Long id;
    private Long farmId;
    private String itemName;
    private String category;
    private Double quantityKg;

    public static InventoryItemResponse from(InventoryItem item) {
        InventoryItemResponse r = new InventoryItemResponse();
        r.id = item.getId();
        r.farmId = item.getFarm().getId();
        r.itemName = item.getItemName();
        r.category = item.getCategory();
        r.quantityKg = item.getQuantityKg();
        return r;
    }

    public Long getId() { return id; }
    public Long getFarmId() { return farmId; }
    public String getItemName() { return itemName; }
    public String getCategory() { return category; }
    public Double getQuantityKg() { return quantityKg; }
}
