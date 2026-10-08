package com.agriconnect.inventory.service;

import com.agriconnect.common.exception.NotFoundException;
import com.agriconnect.common.security.SecurityUtils;
import com.agriconnect.farm.entity.Farm;
import com.agriconnect.farm.service.FarmService;
import com.agriconnect.inventory.dto.InventoryItemRequest;
import com.agriconnect.inventory.dto.InventoryItemResponse;
import com.agriconnect.inventory.entity.InventoryItem;
import com.agriconnect.inventory.repository.InventoryItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final FarmService farmService;

    public InventoryService(InventoryItemRepository inventoryItemRepository, FarmService farmService) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.farmService = farmService;
    }

    @Transactional
    public InventoryItemResponse create(InventoryItemRequest request) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");

        Farm farm = farmService.getOwnedFarm(request.getFarmId());

        InventoryItem item = new InventoryItem();
        item.setTenantId(SecurityUtils.currentTenantId());
        item.setFarm(farm);
        item.setItemName(request.getItemName());
        item.setCategory(request.getCategory());
        item.setQuantityKg(request.getQuantityKg());

        return InventoryItemResponse.from(inventoryItemRepository.save(item));
    }

    public List<InventoryItemResponse> findByFarm(Long farmId) {
        farmService.getOwnedFarm(farmId);
        return inventoryItemRepository.findByFarmId(farmId).stream().map(InventoryItemResponse::from).toList();
    }

    public List<InventoryItemResponse> findAll() {
        return inventoryItemRepository.findAll().stream().map(InventoryItemResponse::from).toList();
    }

    /** Total inventory (kg) across the current tenant - used by the analytics dashboard. */
    public Double totalQuantityKg() {
        Double total = inventoryItemRepository.sumQuantityKg();
        return total != null ? total : 0.0;
    }

    @Transactional
    public InventoryItemResponse update(Long id, InventoryItemRequest request) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");

        InventoryItem item = getOwnedItem(id);
        Farm farm = farmService.getOwnedFarm(request.getFarmId());

        item.setFarm(farm);
        item.setItemName(request.getItemName());
        item.setCategory(request.getCategory());
        item.setQuantityKg(request.getQuantityKg());

        return InventoryItemResponse.from(inventoryItemRepository.save(item));
    }

    @Transactional
    public void delete(Long id) {
        SecurityUtils.requireRole("OWNER", "ADMIN", "FARMER");
        inventoryItemRepository.delete(getOwnedItem(id));
    }

    private InventoryItem getOwnedItem(Long id) {
        return inventoryItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inventory item not found: " + id));
    }
}
