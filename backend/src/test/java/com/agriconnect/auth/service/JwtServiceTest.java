package com.agriconnect.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "test-secret-key-for-junit-tests-only-32-chars-min");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3_600_000L);
    }

    @Test
    void generateToken_thenExtractClaims_returnsWhatWasEncoded() {
        String token = jwtService.generateToken(1L, "farmer@agriconnect.com", 10L, "OWNER");

        assertThat(jwtService.extractUserId(token)).isEqualTo(1L);
        assertThat(jwtService.extractTenantId(token)).isEqualTo(10L);
        assertThat(jwtService.extractRole(token)).isEqualTo("OWNER");
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_withTamperedSignature_returnsFalse() {
        String token = jwtService.generateToken(1L, "x@x.com", 1L, "OWNER");
        String tampered = token.substring(0, token.length() - 2) + "ab";

        assertThat(jwtService.isTokenValid(tampered)).isFalse();
    }

    @Test
    void isTokenValid_withExpiredToken_returnsFalse() throws InterruptedException {
        ReflectionTestUtils.setField(jwtService, "expirationMs", 1L);
        String token = jwtService.generateToken(1L, "x@x.com", 1L, "OWNER");

        Thread.sleep(5);

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }
}
