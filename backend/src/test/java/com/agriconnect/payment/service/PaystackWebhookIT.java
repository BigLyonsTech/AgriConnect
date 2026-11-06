package com.agriconnect.payment.service;

import com.agriconnect.marketplace.entity.Order;
import com.agriconnect.marketplace.entity.OrderStatus;
import com.agriconnect.marketplace.repository.OrderRepository;
import com.agriconnect.payment.entity.EscrowStatus;
import com.agriconnect.payment.entity.EscrowTransaction;
import com.agriconnect.payment.repository.EscrowTransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Signed Paystack webhooks: a correct charge moves escrow PENDING -> HELD,
 * a wrong amount is rejected, and a replay after release changes nothing.
 */
@SpringBootTest(properties = "app.paystack.secret-key=" + PaystackWebhookIT.SECRET)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaystackWebhookIT {

    static final String SECRET = "sk_test_webhook_it_secret";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private OrderRepository orderRepository;
    @Autowired private EscrowTransactionRepository escrowRepository;

    @Test
    void chargeSuccess_isAppliedOnce_rejectsWrongAmount_andIgnoresReplays() throws Exception {
        String farmerToken = demoFarmerToken();
        Order pending = findOrder(farmerToken, "PENDING_PAYMENT");

        EscrowTransaction escrow = new EscrowTransaction();
        escrow.setOrderId(pending.getId());
        escrow.setBuyerOrganizationId(pending.getBuyerOrganizationId());
        escrow.setAmount(pending.getTotalAmount());
        escrow.setPaystackReference("IT-" + UUID.randomUUID());
        escrow.setStatus(EscrowStatus.PENDING);
        escrow = escrowRepository.save(escrow);
        long kobo = pending.getTotalAmount().movePointRight(2).longValueExact();

        webhook(escrow.getPaystackReference(), kobo - 100).andExpect(status().isBadRequest());
        assertThat(escrowStatus(escrow)).isEqualTo(EscrowStatus.PENDING);

        webhook(escrow.getPaystackReference(), kobo).andExpect(status().isOk());
        assertThat(escrowStatus(escrow)).isEqualTo(EscrowStatus.HELD);
        assertThat(orderRepository.findById(pending.getId()).orElseThrow().getStatus()).isEqualTo(OrderStatus.PAID);

        mockMvc.perform(post("/api/marketplace/orders/" + pending.getId() + "/fulfill").header("Authorization", farmerToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/payments/orders/" + pending.getId() + "/release").header("Authorization", farmerToken))
                .andExpect(status().is2xxSuccessful());

        // Paystack retries webhooks: the same event again must not undo the release.
        webhook(escrow.getPaystackReference(), kobo).andExpect(status().isOk());
        assertThat(escrowStatus(escrow)).isEqualTo(EscrowStatus.RELEASED);
        assertThat(orderRepository.findById(pending.getId()).orElseThrow().getStatus()).isEqualTo(OrderStatus.FULFILLED);
    }

    @Test
    void badSignature_isRejected() throws Exception {
        mockMvc.perform(post("/api/payments/webhook").contentType(MediaType.APPLICATION_JSON)
                        .header("X-Paystack-Signature", "00ff").content("{\"event\":\"charge.success\"}"))
                .andExpect(status().isBadRequest());
    }

    private EscrowStatus escrowStatus(EscrowTransaction escrow) {
        return escrowRepository.findById(escrow.getId()).orElseThrow().getStatus();
    }

    private ResultActions webhook(String reference, long kobo) throws Exception {
        String body = "{\"event\":\"charge.success\",\"data\":{\"reference\":\"" + reference + "\",\"amount\":" + kobo + "}}";
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        String signature = HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        return mockMvc.perform(post("/api/payments/webhook").contentType(MediaType.APPLICATION_JSON)
                .header("X-Paystack-Signature", signature).content(body));
    }

    private String demoFarmerToken() throws Exception {
        String body = mockMvc.perform(post("/api/auth/demo")).andReturn().getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(body).get("token").asText();
    }

    private Order findOrder(String farmerToken, String wantedStatus) throws Exception {
        String json = mockMvc.perform(get("/api/marketplace/orders/against-my-listings").header("Authorization", farmerToken))
                .andReturn().getResponse().getContentAsString();
        for (JsonNode node : objectMapper.readTree(json)) {
            if (wantedStatus.equals(node.get("status").asText())) {
                return orderRepository.findById(node.get("id").asLong()).orElseThrow();
            }
        }
        throw new AssertionError("No " + wantedStatus + " order in demo data");
    }
}
