package com.agriconnect.payment.service;

import com.agriconnect.common.exception.NotFoundException;
import com.agriconnect.common.security.SecurityUtils;
import com.agriconnect.marketplace.entity.Order;
import com.agriconnect.marketplace.entity.OrderStatus;
import com.agriconnect.marketplace.repository.OrderRepository;
import com.agriconnect.marketplace.service.MarketplaceService;
import com.agriconnect.payment.client.PaystackClient;
import com.agriconnect.payment.dto.EscrowInitiateRequest;
import com.agriconnect.payment.dto.EscrowInitiateResponse;
import com.agriconnect.payment.entity.EscrowStatus;
import com.agriconnect.payment.entity.EscrowTransaction;
import com.agriconnect.payment.repository.EscrowTransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EscrowService {

    private final EscrowTransactionRepository escrowTransactionRepository;
    private final OrderRepository orderRepository;
    private final MarketplaceService marketplaceService;
    private final PaystackClient paystackClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EscrowService(EscrowTransactionRepository escrowTransactionRepository,
                          OrderRepository orderRepository,
                          MarketplaceService marketplaceService,
                          PaystackClient paystackClient) {
        this.escrowTransactionRepository = escrowTransactionRepository;
        this.orderRepository = orderRepository;
        this.marketplaceService = marketplaceService;
        this.paystackClient = paystackClient;
    }

    @Transactional
    public EscrowInitiateResponse initiatePayment(EscrowInitiateRequest request) {
        SecurityUtils.requireRole("BUYER");

        Order order = marketplaceService.getOrder(request.getOrderId());
        if (!order.getBuyerOrganizationId().equals(SecurityUtils.currentTenantId())) {
            throw new AccessDeniedException("This order does not belong to your organization");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalArgumentException("This order is not awaiting payment");
        }

        String reference = "agc_" + UUID.randomUUID();
        PaystackClient.InitResult result = paystackClient.initializeTransaction(
                request.getPayerEmail(), order.getTotalAmount(), reference);

        EscrowTransaction transaction = new EscrowTransaction();
        transaction.setOrderId(order.getId());
        transaction.setBuyerOrganizationId(SecurityUtils.currentTenantId());
        transaction.setAmount(order.getTotalAmount());
        transaction.setPaystackReference(result.reference());
        transaction.setStatus(EscrowStatus.PENDING);
        escrowTransactionRepository.save(transaction);

        return new EscrowInitiateResponse(result.authorizationUrl(), result.reference(), order.getTotalAmount());
    }

    /**
     * Called by Paystack's servers, not the frontend - no JWT is present, so
     * this endpoint is permitted through Spring Security and authenticates
     * itself purely via the HMAC signature (see PaystackClient).
     */
    @Transactional
    public void handleWebhook(String rawBody, String signatureHeader) {
        if (!paystackClient.verifyWebhookSignature(rawBody, signatureHeader)) {
            throw new IllegalArgumentException("Invalid Paystack webhook signature");
        }

        try {
            JsonNode event = objectMapper.readTree(rawBody);
            String eventType = event.path("event").asText();
            String reference = event.path("data").path("reference").asText();

            if ("charge.success".equals(eventType)) {
                EscrowTransaction transaction = escrowTransactionRepository.findByPaystackReference(reference)
                        .orElseThrow(() -> new NotFoundException("No escrow transaction for reference: " + reference));
                transaction.setStatus(EscrowStatus.HELD);
                escrowTransactionRepository.save(transaction);

                Order order = orderRepository.findById(transaction.getOrderId())
                        .orElseThrow(() -> new NotFoundException("Order not found: " + transaction.getOrderId()));
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new IllegalArgumentException("Malformed webhook payload");
        }
    }

    /**
     * Marks held funds as released to the seller. In a real deployment this
     * would call Paystack's Transfer API to actually move money to the
     * seller's registered account; that call needs a verified payout
     * recipient set up per-organization, which is out of scope here, so this
     * is a state transition that documents the intended flow.
     */
    @Transactional
    public void releaseFunds(Long orderId) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");

        Order order = marketplaceService.getOrder(orderId);
        if (!order.getSellerOrganizationId().equals(SecurityUtils.currentTenantId())) {
            throw new AccessDeniedException("This order does not belong to your organization's listings");
        }

        EscrowTransaction transaction = escrowTransactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("No escrow transaction for order: " + orderId));
        if (transaction.getStatus() != EscrowStatus.HELD) {
            throw new IllegalArgumentException("Funds must be HELD before they can be released");
        }
        transaction.setStatus(EscrowStatus.RELEASED);
        escrowTransactionRepository.save(transaction);
    }
}
