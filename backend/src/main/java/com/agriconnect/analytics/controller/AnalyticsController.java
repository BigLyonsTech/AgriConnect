package com.agriconnect.analytics.controller;

import com.agriconnect.analytics.dto.DashboardSummaryResponse;
import com.agriconnect.analytics.service.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    public DashboardSummaryResponse dashboard() {
        return analyticsService.getDashboardSummary();
    }
}
