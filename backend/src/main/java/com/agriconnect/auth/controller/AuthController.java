package com.agriconnect.auth.controller;

import com.agriconnect.auth.dto.AuthResponse;
import com.agriconnect.auth.dto.LoginRequest;
import com.agriconnect.auth.dto.RegisterRequest;
import com.agriconnect.auth.service.AuthService;
import com.agriconnect.auth.service.DemoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final DemoService demoService;

    public AuthController(AuthService authService, DemoService demoService) {
        this.authService = authService;
        this.demoService = demoService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** One-click live demo: ?as=buyer logs in as the demo buyer, otherwise the demo farmer. */
    @PostMapping("/demo")
    public ResponseEntity<AuthResponse> demo(@RequestParam(name = "as", required = false) String as) {
        return ResponseEntity.ok(demoService.login("buyer".equalsIgnoreCase(as)));
    }
}
