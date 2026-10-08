package com.agriconnect.analytics.service;

import com.agriconnect.analytics.dto.DashboardSummaryResponse;
import com.agriconnect.common.security.SecurityUtils;
import com.agriconnect.farm.repository.CropRepository;
import com.agriconnect.farm.repository.FarmRepository;
import com.agriconnect.finance.service.FinanceService;
import com.agriconnect.inventory.service.InventoryService;
import com.agriconnect.marketplace.entity.ListingStatus;
import com.agriconnect.marketplace.entity.OrderStatus;
import com.agriconnect.marketplace.repository.ListingRepository;
import com.agriconnect.marketplace.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Reads across every module rather than owning any data of its own. Farm,
 * Crop and Inventory counts come "for free" tenant-scoped via the Hibernate
 * filter; Listing/Order counts are scoped explicitly by organizationId since
 * those two entities are deliberately not tenant-filtered (see Listing's
 * class comment).
 */
@Service
public class AnalyticsService {

    private final FarmRepository farmRepository;
    private final CropRepository cropRepository;
    private final InventoryService inventoryService;
    private final FinanceService financeService;
    private final ListingRepository listingRepository;
    private final OrderRepository orderRepository;

    public AnalyticsService(FarmRepository farmRepository, CropRepository cropRepository,
                             InventoryService inventoryService, FinanceService financeService,
                             ListingRepository listingRepository, OrderRepository orderRepository) {
        this.farmRepository = farmRepository;
        this.cropRepository = cropRepository;
        this.inventoryService = inventoryService;
        this.financeService = financeService;
        this.listingRepository = listingRepository;
        this.orderRepository = orderRepository;
    }

    public DashboardSummaryResponse getDashboardSummary() {
        Long tenantId = SecurityUtils.currentTenantId();

        long totalFarms = farmRepository.count();
        long totalCrops = cropRepository.count();
        double totalInventoryKg = inventoryService.totalQuantityKg();
        BigDecimal totalExpenses = financeService.totalExpenses();
        BigDecimal totalRevenue = financeService.totalRevenue();
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);
        long activeListings = listingRepository.countByOrganizationIdAndStatus(tenantId, ListingStatus.ACTIVE);
        long pendingOrders = orderRepository.countBySellerOrganizationIdAndStatus(tenantId, OrderStatus.PENDING_PAYMENT);

        return new DashboardSummaryResponse(totalFarms, totalCrops, totalInventoryKg,
                totalExpenses, totalRevenue, netProfit, activeListings, pendingOrders);
    }
}
