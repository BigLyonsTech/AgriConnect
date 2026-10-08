package com.agriconnect.farm.service;

import com.agriconnect.common.exception.NotFoundException;
import com.agriconnect.common.security.SecurityUtils;
import com.agriconnect.farm.dto.CropRequest;
import com.agriconnect.farm.dto.CropResponse;
import com.agriconnect.farm.entity.Crop;
import com.agriconnect.farm.entity.Farm;
import com.agriconnect.farm.repository.CropRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CropService {

    private final CropRepository cropRepository;
    private final FarmService farmService;

    public CropService(CropRepository cropRepository, FarmService farmService) {
        this.cropRepository = cropRepository;
        this.farmService = farmService;
    }

    @Transactional
    public CropResponse create(CropRequest request) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");

        Farm farm = farmService.getOwnedFarm(request.getFarmId());

        Crop crop = new Crop();
        crop.setTenantId(SecurityUtils.currentTenantId());
        crop.setFarm(farm);
        crop.setCropName(request.getCropName());
        crop.setPlantingDate(request.getPlantingDate());
        crop.setExpectedHarvestDate(request.getExpectedHarvestDate());
        crop.setStatus(request.getStatus());

        return CropResponse.from(cropRepository.save(crop));
    }

    public List<CropResponse> findAll() {
        return cropRepository.findAll().stream().map(CropResponse::from).toList();
    }

    public List<CropResponse> findByFarm(Long farmId) {
        farmService.getOwnedFarm(farmId);
        return cropRepository.findByFarmId(farmId).stream().map(CropResponse::from).toList();
    }

    @Transactional
    public CropResponse update(Long id, CropRequest request) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");

        Crop crop = getOwnedCrop(id);
        Farm farm = farmService.getOwnedFarm(request.getFarmId());

        crop.setFarm(farm);
        crop.setCropName(request.getCropName());
        crop.setPlantingDate(request.getPlantingDate());
        crop.setExpectedHarvestDate(request.getExpectedHarvestDate());
        crop.setStatus(request.getStatus());

        return CropResponse.from(cropRepository.save(crop));
    }

    @Transactional
    public void delete(Long id) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");
        cropRepository.delete(getOwnedCrop(id));
    }

    public Crop getOwnedCrop(Long id) {
        return cropRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Crop not found: " + id));
    }
}
