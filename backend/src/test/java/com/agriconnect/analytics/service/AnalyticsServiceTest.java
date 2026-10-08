package com.agriconnect.analytics.service;

import com.agriconnect.analytics.dto.DashboardSummaryResponse;
import com.agriconnect.farm.repository.CropRepository;
import com.agriconnect.farm.repository.FarmRepository;
import com.agriconnect.finance.service.FinanceService;
import com.agriconnect.inventory.service.InventoryService;
import com.agriconnect.marketplace.entity.ListingStatus;
import com.agriconnect.marketplace.entity.OrderStatus;
import com.agriconnect.marketplace.repository.ListingRepository;
import com.agriconnect.marketplace.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock private FarmRepository farmRepository;
    @Mock private CropRepository cropRepository;
    @Mock private InventoryService inventoryService;
    @Mock private FinanceService financeService;
    @Mock private ListingRepository listingRepository;
    @Mock private OrderRepository orderRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
        com.agriconnect.tenant.TenantContext.clear();
    }

    @Test
    void getDashboardSummary_computesNetProfitAndAggregatesAcrossModules() {
        com.agriconnect.tenant.TenantContext.setTenantId(7L);

        when(farmRepository.count()).thenReturn(3L);
        when(cropRepository.count()).thenReturn(9L);
        when(inventoryService.totalQuantityKg()).thenReturn(4820.0);
        when(financeService.totalExpenses()).thenReturn(BigDecimal.valueOf(150000));
        when(financeService.totalRevenue()).thenReturn(BigDecimal.valueOf(400000));
        when(listingRepository.countByOrganizationIdAndStatus(7L, ListingStatus.ACTIVE)).thenReturn(5L);
        when(orderRepository.countBySellerOrganizationIdAndStatus(7L, OrderStatus.PENDING_PAYMENT)).thenReturn(2L);

        DashboardSummaryResponse summary = analyticsService.getDashboardSummary();

        assertThat(summary.getTotalFarms()).isEqualTo(3L);
        assertThat(summary.getTotalCrops()).isEqualTo(9L);
        assertThat(summary.getTotalInventoryKg()).isEqualTo(4820.0);
        assertThat(summary.getNetProfit()).isEqualByComparingTo(BigDecimal.valueOf(250000));
        assertThat(summary.getActiveListings()).isEqualTo(5L);
        assertThat(summary.getPendingOrdersAsSeller()).isEqualTo(2L);
    }
}
