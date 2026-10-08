package com.agriconnect.payment.controller;

import com.agriconnect.payment.dto.EscrowInitiateRequest;
import com.agriconnect.payment.dto.EscrowInitiateResponse;
import com.agriconnect.payment.service.EscrowService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final EscrowService escrowService;

    public PaymentController(EscrowService escrowService) {
        this.escrowService = escrowService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<EscrowInitiateResponse> initiate(@Valid @RequestBody EscrowInitiateRequest request) {
        return ResponseEntity.ok(escrowService.initiatePayment(request));
    }

    /**
     * Public endpoint - Paystack's servers call this directly, with no JWT.
     * Authenticity is verified via the X-Paystack-Signature HMAC header
     * instead (see PaystackClient.verifyWebhookSignature).
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String rawBody,
                                         @RequestHeader(value = "X-Paystack-Signature", required = false) String signature) {
        escrowService.handleWebhook(rawBody, signature);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/orders/{orderId}/release")
    public ResponseEntity<Void> release(@PathVariable Long orderId) {
        escrowService.releaseFunds(orderId);
        return ResponseEntity.noContent().build();
    }
}
