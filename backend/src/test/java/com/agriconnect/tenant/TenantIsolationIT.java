package com.agriconnect.tenant;

import com.agriconnect.auth.dto.RegisterRequest;
import com.agriconnect.farm.dto.FarmRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Proves the Hibernate tenantFilter (BaseEntity + TenantFilterInterceptor)
 * actually isolates data between organizations through the full request
 * pipeline, including by-id lookups.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TenantIsolationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void farmCreatedByOrgA_isInvisibleToOrgB() throws Exception {
        String tokenA = register("Isolation Org A", "a@isolation.test");
        String tokenB = register("Isolation Org B", "b@isolation.test");

        String created = mockMvc.perform(post("/api/farms")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new FarmRequest("Org A Farm", "Ibadan", 12.5))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long farmId = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(get("/api/farms").header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/farms").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/farms/" + farmId).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    private String register(String org, String email) throws Exception {
        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest(org, email, "SecurePass123", "Test User"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(body);
        return json.get("token").asText();
    }
}
