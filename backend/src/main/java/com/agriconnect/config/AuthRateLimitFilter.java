package com.agriconnect.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limits how often one client IP may call the unauthenticated auth
 * endpoints (login, register, demo), to slow down password guessing and
 * account or demo-reset spam. Fixed window per IP + endpoint; over the
 * limit returns 429 with a Retry-After header in the standard error shape.
 *
 * In-memory, so it is per backend instance. Behind a load balancer this
 * would move to a shared store such as Redis, and the client IP would come
 * from a trusted proxy header.
 */
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthRateLimitFilter.class);
    private static final Set<String> LIMITED_PATHS = Set.of(
            "/api/auth/login", "/api/auth/register", "/api/auth/demo", "/api/auth/demo/reset");
    private static final int CLEANUP_THRESHOLD = 10_000;

    private final int maxRequests;
    private final long windowSeconds;
    private final Clock clock;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(int maxRequests, long windowSeconds, Clock clock) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
        this.clock = clock;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod()) || !LIMITED_PATHS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long now = clock.instant().getEpochSecond();
        if (windows.size() > CLEANUP_THRESHOLD) {
            windows.values().removeIf(w -> w.start + windowSeconds <= now);
        }
        String key = request.getRemoteAddr() + " " + request.getRequestURI();
        Window window = windows.compute(key, (k, w) ->
                (w == null || w.start + windowSeconds <= now) ? new Window(now, 1) : new Window(w.start, w.count + 1));

        if (window.count > maxRequests) {
            long retryAfter = Math.max(1, window.start + windowSeconds - now);
            log.warn("Rate limit hit: {} {} from {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"timestamp\":\"" + Instant.now(clock) + "\",\"status\":429,"
                    + "\"error\":\"Too Many Requests\",\"message\":\"Too many attempts. Please wait "
                    + retryAfter + " seconds and try again.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private record Window(long start, int count) {
    }
}
