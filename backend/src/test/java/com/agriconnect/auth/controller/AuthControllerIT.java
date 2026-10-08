package com.agriconnect.auth.controller;

import com.agriconnect.auth.dto.LoginRequest;
import com.agriconnect.auth.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test through the real Spring context, security filter chain,
 * and an in-memory H2 database (see application-test.yml). Proves the full
 * register -> login flow works, not just the individual units.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerThenLogin_fullFlow_succeeds() throws Exception {
        RegisterRequest request = new RegisterRequest("Sunrise Cooperative", "admin@sunrise.coop", "SecurePass123", "Kwame Mensah");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("OWNER"))
                .andExpect(jsonPath("$.organizationName").value("Sunrise Cooperative"));

        LoginRequest login = new LoginRequest("admin@sunrise.coop", "SecurePass123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void register_withDuplicateEmail_returnsBadRequest() throws Exception {
        RegisterRequest first = new RegisterRequest("Org One", "dup@x.com", "SecurePass123", "Name One");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        RegisterRequest duplicate = new RegisterRequest("Org Two", "dup@x.com", "SecurePass123", "Name Two");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withInvalidPayload_returnsValidationErrors() throws Exception {
        RegisterRequest invalid = new RegisterRequest("", "not-an-email", "short", "");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    void login_withWrongPassword_returnsUnauthorized() throws Exception {
        RegisterRequest request = new RegisterRequest("Org Three", "user3@x.com", "SecurePass123", "Name Three");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        LoginRequest wrongLogin = new LoginRequest("user3@x.com", "WrongPassword1");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongLogin)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anyEndpointOutsideAuth_withoutToken_isRejected() throws Exception {
        // /api/farms is a real, protected endpoint now - this proves the security
        // chain itself rejects unauthenticated requests before routing even happens.
        mockMvc.perform(get("/api/farms"))
                .andExpect(status().isUnauthorized());
    }
}
