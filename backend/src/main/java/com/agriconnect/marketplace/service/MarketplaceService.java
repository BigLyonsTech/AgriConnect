package com.agriconnect.marketplace.service;

import com.agriconnect.common.exception.NotFoundException;
import com.agriconnect.common.security.SecurityUtils;
import com.agriconnect.marketplace.dto.ListingRequest;
import com.agriconnect.marketplace.dto.ListingResponse;
import com.agriconnect.marketplace.dto.OrderResponse;
import com.agriconnect.marketplace.dto.PlaceOrderRequest;
import com.agriconnect.marketplace.entity.Listing;
import com.agriconnect.marketplace.entity.ListingStatus;
import com.agriconnect.marketplace.entity.Order;
import com.agriconnect.marketplace.entity.OrderStatus;
import com.agriconnect.marketplace.repository.ListingRepository;
import com.agriconnect.marketplace.repository.OrderRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MarketplaceService {

    private final ListingRepository listingRepository;
    private final OrderRepository orderRepository;

    public MarketplaceService(ListingRepository listingRepository, OrderRepository orderRepository) {
        this.listingRepository = listingRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public ListingResponse createListing(ListingRequest request) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");

        Listing listing = new Listing();
        listing.setOrganizationId(SecurityUtils.currentTenantId());
        listing.setFarmId(request.getFarmId());
        listing.setProduceName(request.getProduceName());
        listing.setDescription(request.getDescription());
        listing.setPricePerUnit(request.getPricePerUnit());
        listing.setUnit(request.getUnit());
        listing.setQuantityAvailable(request.getQuantityAvailable());
        listing.setStatus(ListingStatus.ACTIVE);

        return ListingResponse.from(listingRepository.save(listing));
    }

    /** Public marketplace browse - deliberately NOT tenant-scoped. Any authenticated user sees every active listing. */
    public List<ListingResponse> browseActiveListings() {
        return listingRepository.findByStatus(ListingStatus.ACTIVE).stream().map(ListingResponse::from).toList();
    }

    /** "My listings" - the seller's own view, explicitly filtered by organizationId (not the Hibernate filter, since Listing isn't a BaseEntity). */
    public List<ListingResponse> myListings() {
        return listingRepository.findByOrganizationId(SecurityUtils.currentTenantId())
                .stream().map(ListingResponse::from).toList();
    }

    @Transactional
    public void closeListing(Long listingId) {
        Listing listing = getListing(listingId);
        requireOwnListing(listing);
        listing.setStatus(ListingStatus.CLOSED);
        listingRepository.save(listing);
    }

    /**
     * Places an order against a listing. This is the core cross-tenant
     * transaction: buyer and seller belong to different organizations, the
     * quantity is validated and deducted, and the listing flips to SOLD_OUT
     * once it hits zero.
     */
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        SecurityUtils.requireRole("BUYER");

        Listing listing = getListing(request.getListingId());
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalArgumentException("This listing is no longer active");
        }
        if (listing.getQuantityAvailable().compareTo(request.getQuantity()) < 0) {
            throw new IllegalArgumentException("Not enough quantity available on this listing");
        }

        BigDecimal remaining = listing.getQuantityAvailable().subtract(request.getQuantity());
        listing.setQuantityAvailable(remaining);
        if (remaining.compareTo(BigDecimal.ZERO) == 0) {
            listing.setStatus(ListingStatus.SOLD_OUT);
        }
        listingRepository.save(listing);

        Order order = new Order();
        order.setListingId(listing.getId());
        order.setBuyerOrganizationId(SecurityUtils.currentTenantId());
        order.setSellerOrganizationId(listing.getOrganizationId());
        order.setQuantity(request.getQuantity());
        order.setTotalAmount(listing.getPricePerUnit().multiply(request.getQuantity()));
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        return OrderResponse.from(orderRepository.save(order));
    }

    /** Buyer's own purchase history. */
    public List<OrderResponse> myOrdersAsBuyer() {
        return orderRepository.findByBuyerOrganizationId(SecurityUtils.currentTenantId())
                .stream().map(OrderResponse::from).toList();
    }

    /** Seller's view of orders placed against their listings. */
    public List<OrderResponse> ordersAgainstMyListings() {
        return orderRepository.findBySellerOrganizationId(SecurityUtils.currentTenantId())
                .stream().map(OrderResponse::from).toList();
    }

    @Transactional
    public OrderResponse markFulfilled(Long orderId) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");
        Order order = getOrder(orderId);
        if (!order.getSellerOrganizationId().equals(SecurityUtils.currentTenantId())) {
            throw new AccessDeniedException("This order does not belong to your organization's listings");
        }
        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalArgumentException("Only paid orders can be marked fulfilled");
        }
        order.setStatus(OrderStatus.FULFILLED);
        return OrderResponse.from(orderRepository.save(order));
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
    }

    private Listing getListing(Long id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Listing not found: " + id));
    }

    private void requireOwnListing(Listing listing) {
        if (!listing.getOrganizationId().equals(SecurityUtils.currentTenantId())) {
            throw new AccessDeniedException("This listing does not belong to your organization");
        }
    }
}
