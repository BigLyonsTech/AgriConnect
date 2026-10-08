package com.agriconnect.payment.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Not a BaseEntity for the same reason as Order/Listing - it's scoped by an
 * explicit buyerOrganizationId, checked in EscrowService, rather than the
 * blanket Hibernate tenantFilter.
 */
@Entity
@Table(name = "escrow_transactions")
public class EscrowTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "buyer_organization_id", nullable = false)
    private Long buyerOrganizationId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "paystack_reference", nullable = false, unique = true)
    private String paystackReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EscrowStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getBuyerOrganizationId() { return buyerOrganizationId; }
    public void setBuyerOrganizationId(Long buyerOrganizationId) { this.buyerOrganizationId = buyerOrganizationId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPaystackReference() { return paystackReference; }
    public void setPaystackReference(String paystackReference) { this.paystackReference = paystackReference; }
    public EscrowStatus getStatus() { return status; }
    public void setStatus(EscrowStatus status) { this.status = status; }
}
