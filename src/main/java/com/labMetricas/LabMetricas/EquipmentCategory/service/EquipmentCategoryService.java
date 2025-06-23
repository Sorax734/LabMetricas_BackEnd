package com.labMetricas.LabMetricas.EquipmentCategory.service;

import com.labMetricas.LabMetricas.EquipmentCategory.model.EquipmentCategory;
import com.labMetricas.LabMetricas.EquipmentCategory.model.dto.EquipmentCategoryDto;
import com.labMetricas.LabMetricas.EquipmentCategory.repository.EquipmentCategoryRepository;
import com.labMetricas.LabMetricas.util.ResponseObject;
import com.labMetricas.LabMetricas.enums.TypeResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipmentCategoryService {
    private final EquipmentCategoryRepository equipmentCategoryRepository;

    // Create a new Equipment Category (always set status to true)
    @Transactional
    public ResponseObject createEquipmentCategory(EquipmentCategoryDto categoryDto) {
        // Check if category with same name already exists
        if (equipmentCategoryRepository.existsByNameIgnoreCase(categoryDto.getName())) {
            return new ResponseObject("Category with this name already exists", TypeResponse.ERROR);
        }

        // Create new category (always set status to true)
        EquipmentCategory category = new EquipmentCategory();
        category.setName(categoryDto.getName());
        category.setStatus(true);  // Always set to true when creating

        EquipmentCategory savedCategory = equipmentCategoryRepository.save(category);

        // Convert to DTO for response
        EquipmentCategoryDto savedDto = convertToDto(savedCategory);
        return new ResponseObject("Category created successfully", savedDto, TypeResponse.SUCCESS);
    }

    // Toggle Equipment Category status
    @Transactional
    public ResponseObject toggleEquipmentCategoryStatus(UUID id) {
        EquipmentCategory category = equipmentCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));

        // Toggle the status
        category.setStatus(!category.getStatus());
        EquipmentCategory updatedCategory = equipmentCategoryRepository.save(category);

        return new ResponseObject("Category status toggled successfully", convertToDto(updatedCategory), TypeResponse.SUCCESS);
    }

    // Get all Equipment Categories
    @Transactional(readOnly = true)
    public ResponseObject getAllEquipmentCategories() {
        List<EquipmentCategoryDto> categories = equipmentCategoryRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseObject("Categories retrieved successfully", categories, TypeResponse.SUCCESS);
    }

    // Get active Equipment Categories
    @Transactional(readOnly = true)
    public ResponseObject getActiveEquipmentCategories() {
        List<EquipmentCategoryDto> activeCategories = equipmentCategoryRepository.findByStatusTrue()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseObject("Active categories retrieved successfully", activeCategories, TypeResponse.SUCCESS);
    }

    // Get Equipment Category by ID
    @Transactional(readOnly = true)
    public ResponseObject getEquipmentCategoryById(UUID id) {
        EquipmentCategory category = equipmentCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
        return new ResponseObject("Category retrieved successfully", convertToDto(category), TypeResponse.SUCCESS);
    }

    // Update Equipment Category (allow updating name, but not status directly)
    @Transactional
    public ResponseObject updateEquipmentCategory(UUID id, EquipmentCategoryDto categoryDto) {
        EquipmentCategory existingCategory = equipmentCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));

        // Check if new name already exists (excluding current category)
        if (equipmentCategoryRepository.findByNameIgnoreCase(categoryDto.getName())
                .map(cat -> !cat.getId().equals(id))
                .orElse(false)) {
            return new ResponseObject("Category with this name already exists", TypeResponse.ERROR);
        }

        // Update only the name
        existingCategory.setName(categoryDto.getName());

        EquipmentCategory updatedCategory = equipmentCategoryRepository.save(existingCategory);
        return new ResponseObject("Category updated successfully", convertToDto(updatedCategory), TypeResponse.SUCCESS);
    }

    // Delete Equipment Category (soft delete)
    @Transactional
    public ResponseObject deleteEquipmentCategory(UUID id) {
        EquipmentCategory category = equipmentCategoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));

        category.setStatus(false);
        equipmentCategoryRepository.save(category);
        return new ResponseObject("Category soft deleted successfully", TypeResponse.SUCCESS);
    }

    // Utility method to convert Entity to DTO
    private EquipmentCategoryDto convertToDto(EquipmentCategory category) {
        return new EquipmentCategoryDto(
                category.getId(),
                category.getName(),
                category.getStatus()
        );
    }
} 