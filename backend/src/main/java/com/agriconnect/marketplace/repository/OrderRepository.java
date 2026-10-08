package com.agriconnect.marketplace.repository;

import com.agriconnect.marketplace.entity.Order;
import com.agriconnect.marketplace.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyerOrganizationId(Long buyerOrganizationId);
    List<Order> findBySellerOrganizationId(Long sellerOrganizationId);
    long countBySellerOrganizationIdAndStatus(Long sellerOrganizationId, OrderStatus status);
}
