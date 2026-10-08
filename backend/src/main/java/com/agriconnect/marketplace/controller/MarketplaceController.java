package com.agriconnect.marketplace.controller;

import com.agriconnect.marketplace.dto.*;
import com.agriconnect.marketplace.service.MarketplaceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace")
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    public MarketplaceController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @PostMapping("/listings")
    public ResponseEntity<ListingResponse> createListing(@Valid @RequestBody ListingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(marketplaceService.createListing(request));
    }

    /** Public - no auth required. Anyone can browse active produce listings. */
    @GetMapping("/listings")
    public List<ListingResponse> browseListings() {
        return marketplaceService.browseActiveListings();
    }

    @GetMapping("/listings/mine")
    public List<ListingResponse> myListings() {
        return marketplaceService.myListings();
    }

    @DeleteMapping("/listings/{id}")
    public ResponseEntity<Void> closeListing(@PathVariable Long id) {
        marketplaceService.closeListing(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(marketplaceService.placeOrder(request));
    }

    @GetMapping("/orders/mine")
    public List<OrderResponse> myOrders() {
        return marketplaceService.myOrdersAsBuyer();
    }

    @GetMapping("/orders/against-my-listings")
    public List<OrderResponse> ordersAgainstMyListings() {
        return marketplaceService.ordersAgainstMyListings();
    }

    @PostMapping("/orders/{id}/fulfill")
    public OrderResponse markFulfilled(@PathVariable Long id) {
        return marketplaceService.markFulfilled(id);
    }
}
