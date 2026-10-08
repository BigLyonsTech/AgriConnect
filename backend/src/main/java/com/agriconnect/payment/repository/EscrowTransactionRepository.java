package com.agriconnect.payment.repository;

import com.agriconnect.payment.entity.EscrowTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EscrowTransactionRepository extends JpaRepository<EscrowTransaction, Long> {
    Optional<EscrowTransaction> findByPaystackReference(String reference);
    Optional<EscrowTransaction> findByOrderId(Long orderId);
    List<EscrowTransaction> findByBuyerOrganizationId(Long buyerOrganizationId);
}
