package com.agriconnect.inventory.service;

import com.agriconnect.common.exception.NotFoundException;
import com.agriconnect.common.security.SecurityUtils;
import com.agriconnect.farm.entity.Crop;
import com.agriconnect.farm.service.CropService;
import com.agriconnect.inventory.dto.YieldRecordRequest;
import com.agriconnect.inventory.dto.YieldRecordResponse;
import com.agriconnect.inventory.entity.YieldRecord;
import com.agriconnect.inventory.repository.YieldRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class YieldService {

    private final YieldRecordRepository yieldRecordRepository;
    private final CropService cropService;

    public YieldService(YieldRecordRepository yieldRecordRepository, CropService cropService) {
        this.yieldRecordRepository = yieldRecordRepository;
        this.cropService = cropService;
    }

    @Transactional
    public YieldRecordResponse create(YieldRecordRequest request) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");

        Crop crop = cropService.getOwnedCrop(request.getCropId());

        YieldRecord record = new YieldRecord();
        record.setTenantId(SecurityUtils.currentTenantId());
        record.setCrop(crop);
        record.setHarvestDate(request.getHarvestDate());
        record.setQuantityKg(request.getQuantityKg());

        return YieldRecordResponse.from(yieldRecordRepository.save(record));
    }

    public List<YieldRecordResponse> findByCrop(Long cropId) {
        cropService.getOwnedCrop(cropId);
        return yieldRecordRepository.findByCropId(cropId).stream().map(YieldRecordResponse::from).toList();
    }

    public List<YieldRecordResponse> findAll() {
        return yieldRecordRepository.findAll().stream().map(YieldRecordResponse::from).toList();
    }

    @Transactional
    public void delete(Long id) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");
        YieldRecord record = yieldRecordRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Yield record not found: " + id));
        yieldRecordRepository.delete(record);
    }
}
