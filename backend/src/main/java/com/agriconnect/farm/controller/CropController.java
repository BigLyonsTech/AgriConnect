package com.agriconnect.farm.controller;

import com.agriconnect.farm.dto.CropRequest;
import com.agriconnect.farm.dto.CropResponse;
import com.agriconnect.farm.service.CropService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crops")
public class CropController {

    private final CropService cropService;

    public CropController(CropService cropService) {
        this.cropService = cropService;
    }

    @PostMapping
    public ResponseEntity<CropResponse> create(@Valid @RequestBody CropRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cropService.create(request));
    }

    @GetMapping
    public List<CropResponse> findAll(@RequestParam(required = false) Long farmId) {
        return farmId != null ? cropService.findByFarm(farmId) : cropService.findAll();
    }

    @PutMapping("/{id}")
    public CropResponse update(@PathVariable Long id, @Valid @RequestBody CropRequest request) {
        return cropService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cropService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
