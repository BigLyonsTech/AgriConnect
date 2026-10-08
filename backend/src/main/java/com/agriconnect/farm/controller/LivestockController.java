package com.agriconnect.farm.controller;

import com.agriconnect.farm.dto.LivestockRequest;
import com.agriconnect.farm.dto.LivestockResponse;
import com.agriconnect.farm.service.LivestockService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/livestock")
public class LivestockController {

    private final LivestockService livestockService;

    public LivestockController(LivestockService livestockService) {
        this.livestockService = livestockService;
    }

    @PostMapping
    public ResponseEntity<LivestockResponse> create(@Valid @RequestBody LivestockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(livestockService.create(request));
    }

    @GetMapping
    public List<LivestockResponse> findAll(@RequestParam(required = false) Long farmId) {
        return farmId != null ? livestockService.findByFarm(farmId) : livestockService.findAll();
    }

    @PutMapping("/{id}")
    public LivestockResponse update(@PathVariable Long id, @Valid @RequestBody LivestockRequest request) {
        return livestockService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        livestockService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
