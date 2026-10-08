package com.agriconnect.payment.dto;

import com.agriconnect.payment.entity.EscrowTransaction;

import java.math.BigDecimal;

public class EscrowTransactionResponse {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String paystackReference;
    private String status;

    public static EscrowTransactionResponse from(EscrowTransaction tx) {
        EscrowTransactionResponse r = new EscrowTransactionResponse();
        r.id = tx.getId();
        r.orderId = tx.getOrderId();
        r.amount = tx.getAmount();
        r.paystackReference = tx.getPaystackReference();
        r.status = tx.getStatus().name();
        return r;
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public String getPaystackReference() { return paystackReference; }
    public String getStatus() { return status; }
}
