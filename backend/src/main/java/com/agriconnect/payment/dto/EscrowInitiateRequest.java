package com.agriconnect.payment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EscrowInitiateRequest {
    @NotNull(message = "Order id is required")
    private Long orderId;

    @NotBlank(message = "Payer email is required")
    @Email(message = "Payer email must be valid")
    private String payerEmail;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getPayerEmail() { return payerEmail; }
    public void setPayerEmail(String payerEmail) { this.payerEmail = payerEmail; }
}
