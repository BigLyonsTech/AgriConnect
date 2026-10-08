package com.agriconnect.finance.dto;

import com.agriconnect.finance.entity.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseResponse {
    private Long id;
    private Long farmId;
    private String category;
    private BigDecimal amount;
    private String description;
    private LocalDate date;

    public static ExpenseResponse from(Expense e) {
        ExpenseResponse r = new ExpenseResponse();
        r.id = e.getId();
        r.farmId = e.getFarm().getId();
        r.category = e.getCategory();
        r.amount = e.getAmount();
        r.description = e.getDescription();
        r.date = e.getDate();
        return r;
    }

    public Long getId() { return id; }
    public Long getFarmId() { return farmId; }
    public String getCategory() { return category; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }
}
