package com.agriconnect.payment.dto;

import java.math.BigDecimal;

public class EscrowInitiateResponse {
    private String authorizationUrl;
    private String reference;
    private BigDecimal amount;

    public EscrowInitiateResponse(String authorizationUrl, String reference, BigDecimal amount) {
        this.authorizationUrl = authorizationUrl;
        this.reference = reference;
        this.amount = amount;
    }

    public String getAuthorizationUrl() { return authorizationUrl; }
    public String getReference() { return reference; }
    public BigDecimal getAmount() { return amount; }
}
