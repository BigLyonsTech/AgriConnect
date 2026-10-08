package com.agriconnect.marketplace.repository;

import com.agriconnect.marketplace.entity.Listing;
import com.agriconnect.marketplace.entity.ListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long> {
    List<Listing> findByStatus(ListingStatus status);
    List<Listing> findByOrganizationId(Long organizationId);
    long countByOrganizationIdAndStatus(Long organizationId, ListingStatus status);
}
