package com.agriconnect.marketplace.service;

import com.agriconnect.marketplace.dto.ListingRequest;
import com.agriconnect.marketplace.dto.ListingResponse;
import com.agriconnect.marketplace.dto.OrderResponse;
import com.agriconnect.marketplace.dto.PlaceOrderRequest;
import com.agriconnect.marketplace.entity.Listing;
import com.agriconnect.marketplace.entity.ListingStatus;
import com.agriconnect.marketplace.entity.Order;
import com.agriconnect.marketplace.repository.ListingRepository;
import com.agriconnect.marketplace.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketplaceServiceTest {

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private MarketplaceService marketplaceService;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
        com.agriconnect.tenant.TenantContext.clear();
    }

    private void authenticateAs(Long tenantId, String role) {
        var auth = new UsernamePasswordAuthenticationToken(1L, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(auth);
        com.agriconnect.tenant.TenantContext.setTenantId(tenantId);
    }

    @Test
    void createListing_asFarmer_setsOrganizationIdAndActiveStatus() {
        authenticateAs(1L, "FARMER");
        when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> {
            Listing l = inv.getArgument(0);
            l.setId(100L);
            return l;
        });

        ListingRequest request = new ListingRequest();
        request.setProduceName("Tomatoes");
        request.setPricePerUnit(BigDecimal.valueOf(2500));
        request.setUnit("kg");
        request.setQuantityAvailable(BigDecimal.valueOf(50));

        ListingResponse response = marketplaceService.createListing(request);

        assertThat(response.getOrganizationId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void placeOrder_withEnoughStock_deductsQuantityAndComputesTotal() {
        authenticateAs(2L, "BUYER");

        Listing listing = new Listing();
        listing.setId(100L);
        listing.setOrganizationId(1L);
        listing.setPricePerUnit(BigDecimal.valueOf(2500));
        listing.setQuantityAvailable(BigDecimal.valueOf(50));
        listing.setStatus(ListingStatus.ACTIVE);

        when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(500L);
            return o;
        });

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setListingId(100L);
        request.setQuantity(BigDecimal.valueOf(10));

        OrderResponse response = marketplaceService.placeOrder(request);

        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(25000));
        assertThat(response.getBuyerOrganizationId()).isEqualTo(2L);
        assertThat(response.getSellerOrganizationId()).isEqualTo(1L);
        assertThat(listing.getQuantityAvailable()).isEqualByComparingTo(BigDecimal.valueOf(40));
        assertThat(listing.getStatus()).isEqualTo(ListingStatus.ACTIVE);
    }

    @Test
    void placeOrder_exceedingAvailableQuantity_throwsIllegalArgument() {
        authenticateAs(2L, "BUYER");

        Listing listing = new Listing();
        listing.setId(100L);
        listing.setOrganizationId(1L);
        listing.setPricePerUnit(BigDecimal.valueOf(2500));
        listing.setQuantityAvailable(BigDecimal.valueOf(5));
        listing.setStatus(ListingStatus.ACTIVE);

        when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setListingId(100L);
        request.setQuantity(BigDecimal.valueOf(10));

        assertThatThrownBy(() -> marketplaceService.placeOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not enough quantity");
    }

    @Test
    void placeOrder_depletingListingExactly_marksSoldOut() {
        authenticateAs(2L, "BUYER");

        Listing listing = new Listing();
        listing.setId(100L);
        listing.setOrganizationId(1L);
        listing.setPricePerUnit(BigDecimal.valueOf(2500));
        listing.setQuantityAvailable(BigDecimal.valueOf(10));
        listing.setStatus(ListingStatus.ACTIVE);

        when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setListingId(100L);
        request.setQuantity(BigDecimal.valueOf(10));

        marketplaceService.placeOrder(request);

        assertThat(listing.getStatus()).isEqualTo(ListingStatus.SOLD_OUT);
    }
}
