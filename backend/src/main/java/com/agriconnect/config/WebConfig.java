package com.agriconnect.config;

import com.agriconnect.tenant.TenantFilterInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TenantFilterInterceptor tenantFilterInterceptor;

    public WebConfig(TenantFilterInterceptor tenantFilterInterceptor) {
        this.tenantFilterInterceptor = tenantFilterInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Must run AFTER Spring Boot's OpenEntityManagerInViewInterceptor (order 0).
        // OSIV binds the request-scoped EntityManager in its preHandle; if this
        // interceptor runs first it enables the tenant filter on an ephemeral
        // Session that is discarded, and tenant isolation silently breaks.
        registry.addInterceptor(tenantFilterInterceptor).order(Ordered.LOWEST_PRECEDENCE);
    }
}
