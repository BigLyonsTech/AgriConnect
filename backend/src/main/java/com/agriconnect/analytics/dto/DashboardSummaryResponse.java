package com.agriconnect.analytics.dto;

import java.math.BigDecimal;

public class DashboardSummaryResponse {
    private long totalFarms;
    private long totalCrops;
    private double totalInventoryKg;
    private BigDecimal totalExpenses;
    private BigDecimal totalRevenue;
    private BigDecimal netProfit;
    private long activeListings;
    private long pendingOrdersAsSeller;

    public DashboardSummaryResponse(long totalFarms, long totalCrops, double totalInventoryKg,
                                     BigDecimal totalExpenses, BigDecimal totalRevenue, BigDecimal netProfit,
                                     long activeListings, long pendingOrdersAsSeller) {
        this.totalFarms = totalFarms;
        this.totalCrops = totalCrops;
        this.totalInventoryKg = totalInventoryKg;
        this.totalExpenses = totalExpenses;
        this.totalRevenue = totalRevenue;
        this.netProfit = netProfit;
        this.activeListings = activeListings;
        this.pendingOrdersAsSeller = pendingOrdersAsSeller;
    }

    public long getTotalFarms() { return totalFarms; }
    public long getTotalCrops() { return totalCrops; }
    public double getTotalInventoryKg() { return totalInventoryKg; }
    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public BigDecimal getNetProfit() { return netProfit; }
    public long getActiveListings() { return activeListings; }
    public long getPendingOrdersAsSeller() { return pendingOrdersAsSeller; }
}
