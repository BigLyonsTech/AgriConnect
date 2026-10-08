package com.agriconnect.farm.repository;

import com.agriconnect.farm.entity.Livestock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LivestockRepository extends JpaRepository<Livestock, Long> {

    /**
     * Overrides the default em.find()-based lookup, which bypasses Hibernate
     * filters, so the tenantFilter also applies to by-id reads.
     */
    @Query("select e from Livestock e where e.id = :id")
    Optional<Livestock> findById(@Param("id") Long id);

    List<Livestock> findByFarmId(Long farmId);
}
