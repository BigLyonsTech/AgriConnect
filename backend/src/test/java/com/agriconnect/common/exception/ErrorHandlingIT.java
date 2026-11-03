package com.agriconnect.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Client mistakes get their proper 4xx status and a helpful message in the
 * standard error shape; they must never surface as a 500.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ErrorHandlingIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GlobalExceptionHandler handler;

    private String token;

    @BeforeEach
    void login() throws Exception {
        String body = mockMvc.perform(post("/api/auth/demo"))
                .andReturn().getResponse().getContentAsString();
        token = "Bearer " + objectMapper.readTree(body).get("token").asText();
    }

    @Test
    void malformedJson_is400() throws Exception {
        postJson("/api/farms", "{bad")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is missing or is not valid JSON"));
    }

    @Test
    void missingBody_is400() throws Exception {
        mockMvc.perform(post("/api/farms").header("Authorization", token).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void unknownEnumValue_is400_andListsAllowedValues() throws Exception {
        postJson("/api/crops", "{\"farmId\":1,\"cropName\":\"Maize\",\"status\":\"BOGUS\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("'BOGUS' for field 'status'")))
                .andExpect(jsonPath("$.message", containsString("GROWING")));
    }

    @Test
    void nonNumericPathId_is400() throws Exception {
        mockMvc.perform(get("/api/farms/abc").header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value 'abc' for 'id'"));
    }

    @Test
    void unknownRoute_is404() throws Exception {
        mockMvc.perform(get("/api/nope").header("Authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void wrongMethod_is405() throws Exception {
        mockMvc.perform(patch("/api/farms").header("Authorization", token))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").value("Method PATCH is not supported here"));
    }

    @Test
    void wrongContentType_is415() throws Exception {
        mockMvc.perform(post("/api/farms").header("Authorization", token)
                        .contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void absurdNumbers_failValidation() throws Exception {
        postJson("/api/farms", "{\"name\":\"x\",\"location\":\"y\",\"sizeHectares\":1e400}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.sizeHectares").exists());
        postJson("/api/finance/expenses", "{\"farmId\":1,\"category\":\"Seeds\",\"amount\":123456789012.5,\"date\":\"2026-01-01\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.amount").exists());
    }

    @Test
    void orderTotalBeyondColumnLimit_is400() throws Exception {
        String listing = postJson("/api/marketplace/listings",
                "{\"produceName\":\"Gold maize\",\"pricePerUnit\":9999999999.99,\"unit\":\"kg\",\"quantityAvailable\":10}")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long listingId = objectMapper.readTree(listing).get("id").asLong();

        String buyer = mockMvc.perform(post("/api/auth/demo?as=buyer")).andReturn().getResponse().getContentAsString();
        String buyerToken = "Bearer " + objectMapper.readTree(buyer).get("token").asText();
        mockMvc.perform(post("/api/marketplace/orders").header("Authorization", buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"listingId\":" + listingId + ",\"quantity\":2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Order total cannot exceed")));
    }

    @Test
    void unexpectedError_is500_withGenericMessageOnly() {
        var response = handler.handleGeneric(new IllegalStateException("db password is hunter2"),
                new MockHttpServletRequest("GET", "/api/farms"));
        Map<String, Object> body = response.getBody();
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(body).containsEntry("message", "Something went wrong. Please try again.");
        assertThat(body.toString()).doesNotContain("hunter2");
    }

    private ResultActions postJson(String url, String json) throws Exception {
        return mockMvc.perform(post(url).header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }
}
