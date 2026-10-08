package com.agriconnect.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RevenueRequest {
    @NotNull(message = "Farm id is required")
    private Long farmId;
    @NotBlank(message = "Source is required")
    private String source;
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    @NotNull(message = "Date is required")
    private LocalDate date;

    public Long getFarmId() { return farmId; }
    public void setFarmId(Long farmId) { this.farmId = farmId; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
