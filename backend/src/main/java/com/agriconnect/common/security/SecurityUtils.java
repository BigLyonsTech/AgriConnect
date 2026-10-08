package com.agriconnect.common.security;

import com.agriconnect.tenant.TenantContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;

/**
 * Reads the current request's identity out of TenantContext (tenant id) and
 * Spring Security's context (user id as principal, role as the single
 * granted authority set by JwtAuthFilter).
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long currentTenantId() {
        return TenantContext.getTenantId();
    }

    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return null;
        }
        return (Long) auth.getPrincipal();
    }

    public static String currentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        return auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse(null);
    }

    /**
     * Throws AccessDeniedException (mapped to 403 by GlobalExceptionHandler)
     * unless the current user's role is one of the allowed roles.
     */
    public static void requireRole(String... allowedRoles) {
        String role = currentRole();
        if (role == null || Arrays.stream(allowedRoles).noneMatch(role::equals)) {
            throw new AccessDeniedException(
                    "This action requires one of the following roles: " + Arrays.toString(allowedRoles));
        }
    }
}
