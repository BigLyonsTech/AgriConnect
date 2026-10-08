package com.agriconnect.inventory.repository;

import com.agriconnect.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    /**
     * Overrides the default em.find()-based lookup, which bypasses Hibernate
     * filters, so the tenantFilter also applies to by-id reads.
     */
    @Query("select e from InventoryItem e where e.id = :id")
    Optional<InventoryItem> findById(@Param("id") Long id);

    List<InventoryItem> findByFarmId(Long farmId);

    @Query("select coalesce(sum(i.quantityKg), 0) from InventoryItem i")
    Double sumQuantityKg();
}
