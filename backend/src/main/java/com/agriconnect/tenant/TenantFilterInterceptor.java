package com.agriconnect.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Enables the Hibernate "tenantFilter" (defined on BaseEntity) for the current
 * request's EntityManager, scoped to the tenant id resolved by JwtAuthFilter.
 *
 * This relies on spring.jpa.open-in-view=true (Spring Boot default) so the
 * EntityManager bound here stays open for the whole request and is reused by
 * every repository call made afterwards.
 *
 * No tenant-scoped entities exist yet in Sprint 1 (Organization/User are not
 * tenant-filtered), but this wiring is what Farm/Crop/Listing etc. will rely
 * on automatically from Sprint 2 onward.
 */
@Component
public class TenantFilterInterceptor implements HandlerInterceptor {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
        }
        return true;
    }
}
