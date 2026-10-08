package com.agriconnect.payment.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Thin wrapper around the real Paystack REST API. Needs a real secret key
 * (app.paystack.secret-key / PAYSTACK_SECRET_KEY env var) and internet access
 * to actually reach api.paystack.co - both are unavailable in the sandbox
 * this was written in, so this class compiles and is structurally correct
 * but was not exercised against the live API. Test it against Paystack's
 * test-mode keys on your machine before demoing.
 */
@Component
public class PaystackClient {

    private static final String BASE_URL = "https://api.paystack.co";

    @Value("${app.paystack.secret-key:}")
    private String secretKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public record InitResult(String authorizationUrl, String accessCode, String reference) {}

    public InitResult initializeTransaction(String email, BigDecimal amountNaira, String reference) {
        int amountKobo = amountNaira.multiply(BigDecimal.valueOf(100)).intValue();
        String body = String.format(
                "{\"email\":\"%s\",\"amount\":%d,\"reference\":\"%s\"}",
                email, amountKobo, reference
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/transaction/initialize"))
                .header("Authorization", "Bearer " + secretKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = objectMapper.readTree(response.body());
            if (!json.path("status").asBoolean(false)) {
                throw new PaystackException("Paystack initialization failed: " + json.path("message").asText());
            }
            JsonNode data = json.path("data");
            return new InitResult(
                    data.path("authorization_url").asText(),
                    data.path("access_code").asText(),
                    data.path("reference").asText()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaystackException("Could not reach Paystack: " + e.getMessage());
        } catch (java.io.IOException e) {
            throw new PaystackException("Could not reach Paystack: " + e.getMessage());
        }
    }

    /**
     * Verifies the X-Paystack-Signature header on an incoming webhook: it
     * must equal HMAC-SHA512(rawRequestBody, secretKey) in hex.
     */
    public boolean verifyWebhookSignature(String rawBody, String signatureHeader) {
        if (signatureHeader == null || secretKey == null || secretKey.isBlank()) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString().equalsIgnoreCase(signatureHeader);
        } catch (Exception e) {
            return false;
        }
    }

    public static class PaystackException extends RuntimeException {
        public PaystackException(String message) {
            super(message);
        }
    }
}
