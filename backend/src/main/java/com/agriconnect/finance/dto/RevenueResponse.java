package com.agriconnect.finance.dto;

import com.agriconnect.finance.entity.Revenue;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RevenueResponse {
    private Long id;
    private Long farmId;
    private String source;
    private BigDecimal amount;
    private LocalDate date;

    public static RevenueResponse from(Revenue r0) {
        RevenueResponse r = new RevenueResponse();
        r.id = r0.getId();
        r.farmId = r0.getFarm().getId();
        r.source = r0.getSource();
        r.amount = r0.getAmount();
        r.date = r0.getDate();
        return r;
    }

    public Long getId() { return id; }
    public Long getFarmId() { return farmId; }
    public String getSource() { return source; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getDate() { return date; }
}
