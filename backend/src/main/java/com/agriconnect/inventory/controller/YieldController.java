package com.agriconnect.inventory.controller;

import com.agriconnect.inventory.dto.YieldRecordRequest;
import com.agriconnect.inventory.dto.YieldRecordResponse;
import com.agriconnect.inventory.service.YieldService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/yield-records")
public class YieldController {

    private final YieldService yieldService;

    public YieldController(YieldService yieldService) {
        this.yieldService = yieldService;
    }

    @PostMapping
    public ResponseEntity<YieldRecordResponse> create(@Valid @RequestBody YieldRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(yieldService.create(request));
    }

    @GetMapping
    public List<YieldRecordResponse> findAll(@RequestParam(required = false) Long cropId) {
        return cropId != null ? yieldService.findByCrop(cropId) : yieldService.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        yieldService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
