package com.agriconnect.farm.service;

import com.agriconnect.common.exception.NotFoundException;
import com.agriconnect.common.security.SecurityUtils;
import com.agriconnect.farm.dto.LivestockRequest;
import com.agriconnect.farm.dto.LivestockResponse;
import com.agriconnect.farm.entity.Farm;
import com.agriconnect.farm.entity.Livestock;
import com.agriconnect.farm.repository.LivestockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LivestockService {

    private final LivestockRepository livestockRepository;
    private final FarmService farmService;

    public LivestockService(LivestockRepository livestockRepository, FarmService farmService) {
        this.livestockRepository = livestockRepository;
        this.farmService = farmService;
    }

    @Transactional
    public LivestockResponse create(LivestockRequest request) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");

        Farm farm = farmService.getOwnedFarm(request.getFarmId());

        Livestock livestock = new Livestock();
        livestock.setTenantId(SecurityUtils.currentTenantId());
        livestock.setFarm(farm);
        livestock.setSpecies(request.getSpecies());
        livestock.setBreed(request.getBreed());
        livestock.setQuantity(request.getQuantity());
        livestock.setHealthNotes(request.getHealthNotes());

        return LivestockResponse.from(livestockRepository.save(livestock));
    }

    public List<LivestockResponse> findByFarm(Long farmId) {
        farmService.getOwnedFarm(farmId);
        return livestockRepository.findByFarmId(farmId).stream().map(LivestockResponse::from).toList();
    }

    public List<LivestockResponse> findAll() {
        return livestockRepository.findAll().stream().map(LivestockResponse::from).toList();
    }

    @Transactional
    public LivestockResponse update(Long id, LivestockRequest request) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");

        Livestock livestock = getOwnedLivestock(id);
        Farm farm = farmService.getOwnedFarm(request.getFarmId());

        livestock.setFarm(farm);
        livestock.setSpecies(request.getSpecies());
        livestock.setBreed(request.getBreed());
        livestock.setQuantity(request.getQuantity());
        livestock.setHealthNotes(request.getHealthNotes());

        return LivestockResponse.from(livestockRepository.save(livestock));
    }

    @Transactional
    public void delete(Long id) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");
        livestockRepository.delete(getOwnedLivestock(id));
    }

    private Livestock getOwnedLivestock(Long id) {
        return livestockRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Livestock record not found: " + id));
    }
}
