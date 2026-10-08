package com.agriconnect.finance.controller;

import com.agriconnect.finance.dto.ExpenseRequest;
import com.agriconnect.finance.dto.ExpenseResponse;
import com.agriconnect.finance.dto.RevenueRequest;
import com.agriconnect.finance.dto.RevenueResponse;
import com.agriconnect.finance.service.FinanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @PostMapping("/expenses")
    public ResponseEntity<ExpenseResponse> recordExpense(@Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeService.recordExpense(request));
    }

    @GetMapping("/expenses")
    public List<ExpenseResponse> listExpenses(@RequestParam(required = false) Long farmId) {
        return financeService.findExpenses(farmId);
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        financeService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/revenues")
    public ResponseEntity<RevenueResponse> recordRevenue(@Valid @RequestBody RevenueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeService.recordRevenue(request));
    }

    @GetMapping("/revenues")
    public List<RevenueResponse> listRevenues(@RequestParam(required = false) Long farmId) {
        return financeService.findRevenues(farmId);
    }

    @DeleteMapping("/revenues/{id}")
    public ResponseEntity<Void> deleteRevenue(@PathVariable Long id) {
        financeService.deleteRevenue(id);
        return ResponseEntity.noContent().build();
    }
}
