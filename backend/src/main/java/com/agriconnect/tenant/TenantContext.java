package com.agriconnect.tenant;

/**
 * Holds the current request's tenant id in a ThreadLocal.
 * Set by JwtAuthFilter after validating the JWT, read by TenantFilterInterceptor
 * to enable the Hibernate tenantFilter, and cleared at the end of every request.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static Long getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
