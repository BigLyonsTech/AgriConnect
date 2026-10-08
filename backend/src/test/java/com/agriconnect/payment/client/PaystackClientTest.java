package com.agriconnect.payment.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PaystackClientTest {

    private PaystackClient paystackClient;
    private static final String SECRET = "sk_test_1234567890abcdef";

    @BeforeEach
    void setUp() {
        paystackClient = new PaystackClient();
        ReflectionTestUtils.setField(paystackClient, "secretKey", SECRET);
    }

    @Test
    void verifyWebhookSignature_withCorrectSignature_returnsTrue() throws Exception {
        String body = "{\"event\":\"charge.success\",\"data\":{\"reference\":\"agc_123\"}}";
        String validSignature = hmacSha512Hex(body, SECRET);

        assertThat(paystackClient.verifyWebhookSignature(body, validSignature)).isTrue();
    }

    @Test
    void verifyWebhookSignature_withTamperedBody_returnsFalse() throws Exception {
        String originalBody = "{\"event\":\"charge.success\",\"data\":{\"reference\":\"agc_123\"}}";
        String signatureForOriginal = hmacSha512Hex(originalBody, SECRET);

        String tamperedBody = "{\"event\":\"charge.success\",\"data\":{\"reference\":\"agc_999\"}}";

        assertThat(paystackClient.verifyWebhookSignature(tamperedBody, signatureForOriginal)).isFalse();
    }

    @Test
    void verifyWebhookSignature_withMissingHeader_returnsFalse() {
        assertThat(paystackClient.verifyWebhookSignature("{}", null)).isFalse();
    }

    private String hmacSha512Hex(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
