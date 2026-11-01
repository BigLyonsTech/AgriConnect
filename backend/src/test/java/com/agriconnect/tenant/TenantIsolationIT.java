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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Test
    void everyTenantScopedModule_isIsolatedFromOtherOrgs() throws Exception {
        String tokenA = register("Modules Org A", "a@modules.test");
        String tokenB = register("Modules Org B", "b@modules.test");

        long farmId = create(tokenA, "/api/farms", Map.of("name", "A Farm", "location", "Oyo", "sizeHectares", 3.0));
        Map<String, Object> crop = Map.of("farmId", farmId, "cropName", "Maize", "status", "PLANTED");
        Map<String, Object> livestock = Map.of("farmId", farmId, "species", "Goat", "quantity", 4);
        Map<String, Object> item = Map.of("farmId", farmId, "itemName", "Fertilizer", "category", "Input", "quantityKg", 50.0);
        Map<String, Object> expense = Map.of("farmId", farmId, "category", "Seeds", "amount", 1000, "date", "2026-01-10");
        Map<String, Object> revenue = Map.of("farmId", farmId, "source", "Market", "amount", 5000, "date", "2026-02-10");

        long cropId = create(tokenA, "/api/crops", crop);
        long livestockId = create(tokenA, "/api/livestock", livestock);
        long itemId = create(tokenA, "/api/inventory", item);
        long expenseId = create(tokenA, "/api/finance/expenses", expense);
        long revenueId = create(tokenA, "/api/finance/revenues", revenue);
        Map<String, Object> yield = Map.of("cropId", cropId, "harvestDate", "2026-03-01", "quantityKg", 200.0);
        long yieldId = create(tokenA, "/api/yield-records", yield);

        List<String> lists = List.of("/api/farms", "/api/crops", "/api/livestock", "/api/inventory",
                "/api/finance/expenses", "/api/finance/revenues", "/api/yield-records");

        // Org B sees nothing in any module.
        for (String path : lists) {
            mockMvc.perform(get(path).header("Authorization", "Bearer " + tokenB))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        // Org B cannot modify or delete Org A's records by guessing ids.
        expectNotFound(put("/api/farms/" + farmId), tokenB, Map.of("name", "Hijacked", "location", "X", "sizeHectares", 1.0));
        expectNotFound(put("/api/crops/" + cropId), tokenB, crop);
        expectNotFound(put("/api/livestock/" + livestockId), tokenB, livestock);
        expectNotFound(put("/api/inventory/" + itemId), tokenB, item);
        expectNotFound(delete("/api/crops/" + cropId), tokenB, null);
        expectNotFound(delete("/api/livestock/" + livestockId), tokenB, null);
        expectNotFound(delete("/api/inventory/" + itemId), tokenB, null);
        expectNotFound(delete("/api/finance/expenses/" + expenseId), tokenB, null);
        expectNotFound(delete("/api/finance/revenues/" + revenueId), tokenB, null);
        expectNotFound(delete("/api/yield-records/" + yieldId), tokenB, null);
        expectNotFound(delete("/api/farms/" + farmId), tokenB, null);

        // Org B cannot attach its own records to Org A's farm or crop.
        expectNotFound(post("/api/crops"), tokenB, crop);
        expectNotFound(post("/api/livestock"), tokenB, livestock);
        expectNotFound(post("/api/inventory"), tokenB, item);
        expectNotFound(post("/api/finance/expenses"), tokenB, expense);
        expectNotFound(post("/api/finance/revenues"), tokenB, revenue);
        expectNotFound(post("/api/yield-records"), tokenB, yield);

        // Org A's data survived all of the above untouched.
        for (String path : lists) {
            mockMvc.perform(get(path).header("Authorization", "Bearer " + tokenA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
        mockMvc.perform(get("/api/farms/" + farmId).header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("A Farm"));
    }

    private long create(String token, String path, Map<String, Object> body) throws Exception {
        String created = mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(created).get("id").asLong();
    }

    private void expectNotFound(MockHttpServletRequestBuilder request, String token, Map<String, Object> body) throws Exception {
        request.header("Authorization", "Bearer " + token);
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body));
        }
        mockMvc.perform(request).andExpect(status().isNotFound());
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
