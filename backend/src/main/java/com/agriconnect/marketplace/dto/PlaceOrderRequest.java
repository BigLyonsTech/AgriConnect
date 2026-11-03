package com.agriconnect.marketplace.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class PlaceOrderRequest {
    @NotNull(message = "Listing id is required")
    private Long listingId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    @Digits(integer = 10, fraction = 2, message = "Quantity can have at most 10 digits and 2 decimal places")
    private BigDecimal quantity;

    public Long getListingId() { return listingId; }
    public void setListingId(Long listingId) { this.listingId = listingId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
}
