package com.agriconnect.finance.repository;

import com.agriconnect.finance.entity.Revenue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface RevenueRepository extends JpaRepository<Revenue, Long> {

    /**
     * Overrides the default em.find()-based lookup, which bypasses Hibernate
     * filters, so the tenantFilter also applies to by-id reads.
     */
    @Query("select e from Revenue e where e.id = :id")
    Optional<Revenue> findById(@Param("id") Long id);

    List<Revenue> findByFarmId(Long farmId);

    @Query("select coalesce(sum(r.amount), 0) from Revenue r")
    BigDecimal sumAmount();
}
