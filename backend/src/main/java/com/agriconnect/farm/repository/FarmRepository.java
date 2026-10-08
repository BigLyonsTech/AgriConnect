package com.agriconnect.farm.repository;

import com.agriconnect.farm.entity.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface FarmRepository extends JpaRepository<Farm, Long> {

    /**
     * Overrides the default em.find()-based lookup, which bypasses Hibernate
     * filters, so the tenantFilter also applies to by-id reads.
     */
    @Query("select e from Farm e where e.id = :id")
    Optional<Farm> findById(@Param("id") Long id);

}
