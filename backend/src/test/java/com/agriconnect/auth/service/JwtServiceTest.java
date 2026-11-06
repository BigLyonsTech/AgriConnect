package com.agriconnect.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void validateSecret_rejectsMissingShortAndPlaceholderSecrets() {
        for (String bad : new String[]{null, "", "too-short-secret",
                "change-this-in-production-min-32-characters-long"}) {
            ReflectionTestUtils.setField(jwtService, "secret", bad);
            assertThatThrownBy(jwtService::validateSecret)
                    .as("secret %s", bad)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    void validateSecret_acceptsARandom32ByteSecret() {
        ReflectionTestUtils.setField(jwtService, "secret", "Jx8sV0r2Qe5mT7wZ1yB4nC6dF9gH3kLp");
        assertThatCode(jwtService::validateSecret).doesNotThrowAnyException();
    }
}
