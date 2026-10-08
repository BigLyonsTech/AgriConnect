package com.agriconnect.farm.service;

import com.agriconnect.common.exception.NotFoundException;
import com.agriconnect.common.security.SecurityUtils;
import com.agriconnect.farm.dto.FarmRequest;
import com.agriconnect.farm.dto.FarmResponse;
import com.agriconnect.farm.entity.Farm;
import com.agriconnect.farm.repository.FarmRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FarmService {

    private final FarmRepository farmRepository;

    public FarmService(FarmRepository farmRepository) {
        this.farmRepository = farmRepository;
    }

    @Transactional
    public FarmResponse create(FarmRequest request) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");

        Farm farm = new Farm();
        farm.setTenantId(SecurityUtils.currentTenantId());
        farm.setName(request.getName());
        farm.setLocation(request.getLocation());
        farm.setSizeHectares(request.getSizeHectares());

        return FarmResponse.from(farmRepository.save(farm));
    }

    public List<FarmResponse> findAll() {
        return farmRepository.findAll().stream().map(FarmResponse::from).toList();
    }

    public FarmResponse findById(Long id) {
        return FarmResponse.from(getOwnedFarm(id));
    }

    @Transactional
    public FarmResponse update(Long id, FarmRequest request) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");

        Farm farm = getOwnedFarm(id);
        farm.setName(request.getName());
        farm.setLocation(request.getLocation());
        farm.setSizeHectares(request.getSizeHectares());

        return FarmResponse.from(farmRepository.save(farm));
    }

    @Transactional
    public void delete(Long id) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");
        Farm farm = getOwnedFarm(id);
        farmRepository.delete(farm);
    }

    /**
     * Public so any module needing "this farm exists and belongs to my
     * tenant" (Crop, Livestock, Inventory, Finance services) can reuse it.
     * The Hibernate tenantFilter already guarantees the tenant part; this
     * just turns an empty result into a proper 404 instead of a raw Optional.
     */
    public Farm getOwnedFarm(Long id) {
        return farmRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Farm not found: " + id));
    }
}
