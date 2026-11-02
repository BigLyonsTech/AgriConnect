package com.agriconnect.auth.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The one-click demo seeds once, logs in as farmer or buyer, keeps the two
 * demo orgs isolated, and supports the full fulfil -> release escrow flow.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DemoIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void demoLogin_seedsOnce_andSupportsTheGoldenWorkflow() throws Exception {
        JsonNode farmer = demo("");
        JsonNode again = demo("");
        JsonNode buyer = demo("?as=buyer");

        assertThat(farmer.get("role").asText()).isEqualTo("OWNER");
        assertThat(again.get("userId").asLong()).isEqualTo(farmer.get("userId").asLong());
        assertThat(buyer.get("role").asText()).isEqualTo("BUYER");
        assertThat(buyer.get("organizationId").asLong()).isNotEqualTo(farmer.get("organizationId").asLong());

        String farmerToken = "Bearer " + farmer.get("token").asText();
        String buyerToken = "Bearer " + buyer.get("token").asText();

        // Seeded only once, even though the farmer logged in twice.
        mockMvc.perform(get("/api/farms").header("Authorization", farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
        // The buyer org is a separate tenant and sees none of the farmer's farms.
        mockMvc.perform(get("/api/farms").header("Authorization", buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/marketplace/orders/mine").header("Authorization", buyerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        String orders = mockMvc.perform(get("/api/marketplace/orders/against-my-listings")
                        .header("Authorization", farmerToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long paidOrderId = -1;
        for (JsonNode order : objectMapper.readTree(orders)) {
            if ("PAID".equals(order.get("status").asText())) {
                paidOrderId = order.get("id").asLong();
            }
        }
        assertThat(paidOrderId).isPositive();

        mockMvc.perform(post("/api/marketplace/orders/" + paidOrderId + "/fulfill").header("Authorization", farmerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FULFILLED"));
        mockMvc.perform(post("/api/payments/orders/" + paidOrderId + "/release").header("Authorization", farmerToken))
                .andExpect(status().is2xxSuccessful());
    }

    private JsonNode demo(String query) throws Exception {
        String body = mockMvc.perform(post("/api/auth/demo" + query))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }
}
