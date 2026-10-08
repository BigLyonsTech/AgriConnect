package com.agriconnect.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Organization name is required")
    private String organizationName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    /**
     * Optional at registration: defaults to OWNER. Allowed values are OWNER
     * (the first user of a new farm/cooperative organization) and BUYER
     * (a market trader registering to purchase produce). ADMIN and FARMER
     * are not assignable at sign-up.
     */
    private String role;

    public RegisterRequest() {
    }

    public RegisterRequest(String organizationName, String email, String password, String fullName) {
        this.organizationName = organizationName;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    public RegisterRequest(String organizationName, String email, String password, String fullName, String role) {
        this.organizationName = organizationName;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
