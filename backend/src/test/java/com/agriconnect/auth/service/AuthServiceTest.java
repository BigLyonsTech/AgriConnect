package com.agriconnect.auth.service;

import com.agriconnect.auth.dto.AuthResponse;
import com.agriconnect.auth.dto.LoginRequest;
import com.agriconnect.auth.dto.RegisterRequest;
import com.agriconnect.auth.entity.Organization;
import com.agriconnect.auth.entity.Role;
import com.agriconnect.auth.entity.User;
import com.agriconnect.auth.repository.OrganizationRepository;
import com.agriconnect.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_withNewEmailAndOrganization_createsBothAndReturnsToken() {
        RegisterRequest request = new RegisterRequest("Green Valley Farms", "owner@greenvalley.com", "password123", "Ada Lovelace");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(organizationRepository.existsBySlug("green-valley-farms")).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> {
            Organization org = inv.getArgument(0);
            org.setId(1L);
            return org;
        });
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(5L);
            return user;
        });
        when(jwtService.generateToken(5L, "owner@greenvalley.com", 1L, "OWNER")).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo("OWNER");
        assertThat(response.getOrganizationId()).isEqualTo(1L);
        assertThat(response.getOrganizationName()).isEqualTo("Green Valley Farms");
        verify(userRepository).save(any(User.class));
        verify(organizationRepository).save(any(Organization.class));
    }

    @Test
    void register_withAlreadyRegisteredEmail_throwsIllegalArgumentException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        RegisterRequest request = new RegisterRequest("Some Org", "existing@x.com", "password123", "Name");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    void register_withDuplicateOrganizationSlug_throwsIllegalArgumentException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(organizationRepository.existsBySlug("green-valley-farms")).thenReturn(true);
        RegisterRequest request = new RegisterRequest("Green Valley Farms", "new@x.com", "password123", "Name");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Organization name already taken");
    }

    @Test
    void register_withBuyerRole_createsBuyerUserAndReturnsBuyerToken() {
        RegisterRequest request = new RegisterRequest("City Market Traders", "buyer@citymarket.com", "password123", "Ama Serwaa", "BUYER");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(organizationRepository.existsBySlug("city-market-traders")).thenReturn(false);
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> {
            Organization org = inv.getArgument(0);
            org.setId(2L);
            return org;
        });
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(6L);
            return user;
        });
        when(jwtService.generateToken(6L, "buyer@citymarket.com", 2L, "BUYER")).thenReturn("buyer-jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("buyer-jwt-token");
        assertThat(response.getRole()).isEqualTo("BUYER");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.BUYER);
    }

    @Test
    void register_withInvalidRole_throwsIllegalArgumentException() {
        RegisterRequest request = new RegisterRequest("Some Org", "new@x.com", "password123", "Name", "SUPERADMIN");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role must be OWNER or BUYER");
    }

    @Test
    void login_withCorrectCredentials_returnsToken() {
        Organization org = new Organization();
        org.setId(1L);
        org.setName("Green Valley Farms");
        org.setSlug("green-valley-farms");

        User user = new User();
        user.setId(5L);
        user.setEmail("owner@greenvalley.com");
        user.setPassword("hashed-password");
        user.setRole(Role.OWNER);
        user.setOrganization(org);
        user.setFullName("Ada Lovelace");

        when(userRepository.findByEmail("owner@greenvalley.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(5L, "owner@greenvalley.com", 1L, "OWNER")).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest("owner@greenvalley.com", "password123"));

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUserId()).isEqualTo(5L);
    }

    @Test
    void login_withWrongPassword_throwsBadCredentialsException() {
        User user = new User();
        user.setPassword("hashed-password");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), eq("hashed-password"))).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("x@x.com", "wrong-password")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_withUnknownEmail_throwsBadCredentialsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nouser@x.com", "password123")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
