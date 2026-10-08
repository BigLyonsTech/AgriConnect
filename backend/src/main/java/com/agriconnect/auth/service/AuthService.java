package com.agriconnect.auth.service;

import com.agriconnect.auth.dto.AuthResponse;
import com.agriconnect.auth.dto.LoginRequest;
import com.agriconnect.auth.dto.RegisterRequest;
import com.agriconnect.auth.entity.Organization;
import com.agriconnect.auth.entity.Role;
import com.agriconnect.auth.entity.User;
import com.agriconnect.auth.repository.OrganizationRepository;
import com.agriconnect.auth.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                        OrganizationRepository organizationRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Registers a brand new organization (tenant) together with its first
     * user. The first user is OWNER by default; BUYER is also accepted so a
     * market trader can sign up and purchase produce. ADMIN and FARMER are
     * not assignable at sign-up.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        Role role = resolveRegistrationRole(request.getRole());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        String slug = slugify(request.getOrganizationName());
        if (organizationRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Organization name already taken");
        }

        Organization organization = new Organization();
        organization.setName(request.getOrganizationName());
        organization.setSlug(slug);
        organization = organizationRepository.save(organization);

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(role);
        user.setOrganization(organization);
        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), organization.getId(), role.name());

        return new AuthResponse(token, "Bearer", user.getId(), user.getEmail(), user.getFullName(),
                role.name(), organization.getId(), organization.getName());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(),
                user.getOrganization().getId(), user.getRole().name());

        return new AuthResponse(token, "Bearer", user.getId(), user.getEmail(), user.getFullName(),
                user.getRole().name(), user.getOrganization().getId(), user.getOrganization().getName());
    }

    private String slugify(String name) {
        return name.trim().toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private Role resolveRegistrationRole(String rawRole) {
        if (rawRole == null || rawRole.isBlank()) {
            return Role.OWNER;
        }
        if (rawRole.trim().equalsIgnoreCase("OWNER")) {
            return Role.OWNER;
        }
        if (rawRole.trim().equalsIgnoreCase("BUYER")) {
            return Role.BUYER;
        }
        throw new IllegalArgumentException("Role must be OWNER or BUYER");
    }
}
