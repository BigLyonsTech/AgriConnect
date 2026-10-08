package com.agriconnect.farm.controller;

import com.agriconnect.farm.dto.FarmRequest;
import com.agriconnect.farm.dto.FarmResponse;
import com.agriconnect.farm.service.FarmService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms")
public class FarmController {

    private final FarmService farmService;

    public FarmController(FarmService farmService) {
        this.farmService = farmService;
    }

    @PostMapping
    public ResponseEntity<FarmResponse> create(@Valid @RequestBody FarmRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(farmService.create(request));
    }

    @GetMapping
    public List<FarmResponse> findAll() {
        return farmService.findAll();
    }

    @GetMapping("/{id}")
    public FarmResponse findById(@PathVariable Long id) {
        return farmService.findById(id);
    }

    @PutMapping("/{id}")
    public FarmResponse update(@PathVariable Long id, @Valid @RequestBody FarmRequest request) {
        return farmService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        farmService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
