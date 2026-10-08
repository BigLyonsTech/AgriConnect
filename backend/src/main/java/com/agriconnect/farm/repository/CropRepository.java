package com.agriconnect.farm.repository;

import com.agriconnect.farm.entity.Crop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CropRepository extends JpaRepository<Crop, Long> {

    /**
     * Overrides the default em.find()-based lookup, which bypasses Hibernate
     * filters, so the tenantFilter also applies to by-id reads.
     */
    @Query("select e from Crop e where e.id = :id")
    Optional<Crop> findById(@Param("id") Long id);

    List<Crop> findByFarmId(Long farmId);
}
