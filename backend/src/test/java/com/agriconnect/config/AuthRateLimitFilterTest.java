package com.agriconnect.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRateLimitFilterTest {

    private final AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-11-06T10:00:00Z"));
    private final Clock clock = new Clock() {
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now.get(); }
    };
    private final AuthRateLimitFilter filter = new AuthRateLimitFilter(3, 60, clock);

    @Test
    void allowsUpToTheLimit_thenReturns429WithRetryAfter() throws Exception {
        for (int i = 0; i < 3; i++) {
            assertThat(call("POST", "/api/auth/login", "10.0.0.1").getStatus()).isEqualTo(200);
        }
        now.set(now.get().plusSeconds(20));
        MockHttpServletResponse blocked = call("POST", "/api/auth/login", "10.0.0.1");
        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(blocked.getHeader("Retry-After")).isEqualTo("40");
        assertThat(blocked.getContentAsString()).contains("\"status\":429", "Too many attempts");
    }

    @Test
    void windowResets_andLimitsAreSeparatePerIpAndPerEndpoint() throws Exception {
        for (int i = 0; i < 4; i++) {
            call("POST", "/api/auth/login", "10.0.0.2");
        }
        assertThat(call("POST", "/api/auth/login", "10.0.0.2").getStatus()).isEqualTo(429);
        assertThat(call("POST", "/api/auth/login", "10.0.0.3").getStatus()).isEqualTo(200);
        assertThat(call("POST", "/api/auth/register", "10.0.0.2").getStatus()).isEqualTo(200);

        now.set(now.get().plusSeconds(60));
        assertThat(call("POST", "/api/auth/login", "10.0.0.2").getStatus()).isEqualTo(200);
    }

    @Test
    void ignoresOtherEndpointsAndMethods() throws Exception {
        for (int i = 0; i < 10; i++) {
            assertThat(call("GET", "/api/farms", "10.0.0.4").getStatus()).isEqualTo(200);
            assertThat(call("GET", "/api/auth/login", "10.0.0.4").getStatus()).isEqualTo(200);
        }
    }

    private MockHttpServletResponse call(String method, String uri, String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
