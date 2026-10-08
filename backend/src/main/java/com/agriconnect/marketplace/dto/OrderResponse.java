package com.agriconnect.marketplace.dto;

import com.agriconnect.marketplace.entity.Order;

import java.math.BigDecimal;
import java.time.Instant;

public class OrderResponse {
    private Long id;
    private Long listingId;
    private Long buyerOrganizationId;
    private Long sellerOrganizationId;
    private BigDecimal quantity;
    private BigDecimal totalAmount;
    private String status;
    private Instant createdAt;

    public static OrderResponse from(Order order) {
        OrderResponse r = new OrderResponse();
        r.id = order.getId();
        r.listingId = order.getListingId();
        r.buyerOrganizationId = order.getBuyerOrganizationId();
        r.sellerOrganizationId = order.getSellerOrganizationId();
        r.quantity = order.getQuantity();
        r.totalAmount = order.getTotalAmount();
        r.status = order.getStatus().name();
        r.createdAt = order.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public Long getListingId() { return listingId; }
    public Long getBuyerOrganizationId() { return buyerOrganizationId; }
    public Long getSellerOrganizationId() { return sellerOrganizationId; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
