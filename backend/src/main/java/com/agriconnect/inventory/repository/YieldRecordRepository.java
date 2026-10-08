package com.agriconnect.inventory.repository;

import com.agriconnect.inventory.entity.YieldRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface YieldRecordRepository extends JpaRepository<YieldRecord, Long> {

    /**
     * Overrides the default em.find()-based lookup, which bypasses Hibernate
     * filters, so the tenantFilter also applies to by-id reads.
     */
    @Query("select e from YieldRecord e where e.id = :id")
    Optional<YieldRecord> findById(@Param("id") Long id);

    List<YieldRecord> findByCropId(Long cropId);
}
