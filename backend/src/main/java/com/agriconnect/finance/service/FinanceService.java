package com.agriconnect.finance.service;

import com.agriconnect.common.exception.NotFoundException;
import com.agriconnect.common.security.SecurityUtils;
import com.agriconnect.farm.entity.Farm;
import com.agriconnect.farm.service.FarmService;
import com.agriconnect.finance.dto.ExpenseRequest;
import com.agriconnect.finance.dto.ExpenseResponse;
import com.agriconnect.finance.dto.RevenueRequest;
import com.agriconnect.finance.dto.RevenueResponse;
import com.agriconnect.finance.entity.Expense;
import com.agriconnect.finance.entity.Revenue;
import com.agriconnect.finance.repository.ExpenseRepository;
import com.agriconnect.finance.repository.RevenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FinanceService {

    private final ExpenseRepository expenseRepository;
    private final RevenueRepository revenueRepository;
    private final FarmService farmService;

    public FinanceService(ExpenseRepository expenseRepository, RevenueRepository revenueRepository, FarmService farmService) {
        this.expenseRepository = expenseRepository;
        this.revenueRepository = revenueRepository;
        this.farmService = farmService;
    }

    @Transactional
    public ExpenseResponse recordExpense(ExpenseRequest request) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");
        Farm farm = farmService.getOwnedFarm(request.getFarmId());

        Expense expense = new Expense();
        expense.setTenantId(SecurityUtils.currentTenantId());
        expense.setFarm(farm);
        expense.setCategory(request.getCategory());
        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setDate(request.getDate());

        return ExpenseResponse.from(expenseRepository.save(expense));
    }

    @Transactional
    public RevenueResponse recordRevenue(RevenueRequest request) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");
        Farm farm = farmService.getOwnedFarm(request.getFarmId());

        Revenue revenue = new Revenue();
        revenue.setTenantId(SecurityUtils.currentTenantId());
        revenue.setFarm(farm);
        revenue.setSource(request.getSource());
        revenue.setAmount(request.getAmount());
        revenue.setDate(request.getDate());

        return RevenueResponse.from(revenueRepository.save(revenue));
    }

    public List<ExpenseResponse> findExpenses(Long farmId) {
        List<Expense> expenses = farmId != null ? expenseRepository.findByFarmId(farmId) : expenseRepository.findAll();
        return expenses.stream().map(ExpenseResponse::from).toList();
    }

    public List<RevenueResponse> findRevenues(Long farmId) {
        List<Revenue> revenues = farmId != null ? revenueRepository.findByFarmId(farmId) : revenueRepository.findAll();
        return revenues.stream().map(RevenueResponse::from).toList();
    }

    /** Tenant-wide totals, used directly by the analytics dashboard. */
    public BigDecimal totalExpenses() {
        return expenseRepository.sumAmount();
    }

    public BigDecimal totalRevenue() {
        return revenueRepository.sumAmount();
    }

    @Transactional
    public void deleteExpense(Long id) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));
        expenseRepository.delete(expense);
    }

    @Transactional
    public void deleteRevenue(Long id) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");
        Revenue revenue = revenueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Revenue not found: " + id));
        revenueRepository.delete(revenue);
    }
}
