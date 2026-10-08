package com.agriconnect.finance.repository;

import com.agriconnect.finance.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * Overrides the default em.find()-based lookup, which bypasses Hibernate
     * filters, so the tenantFilter also applies to by-id reads.
     */
    @Query("select e from Expense e where e.id = :id")
    Optional<Expense> findById(@Param("id") Long id);

    List<Expense> findByFarmId(Long farmId);

    @Query("select coalesce(sum(e.amount), 0) from Expense e")
    BigDecimal sumAmount();
}
