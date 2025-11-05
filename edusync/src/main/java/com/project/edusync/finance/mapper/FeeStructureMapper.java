package com.project.edusync.finance.mapper;

import com.project.edusync.finance.dto.feestructure.FeeParticularResponseDTO;
import com.project.edusync.finance.dto.feestructure.FeeStructureResponseDTO;
import com.project.edusync.finance.model.entity.FeeParticular;
import com.project.edusync.finance.model.entity.FeeStructure;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FeeStructureMapper {

    /**
     * Converts a FeeStructure entity and its particulars to a Response DTO.
     */
    public FeeStructureResponseDTO toDto(FeeStructure structure, List<FeeParticular> particulars) {
        FeeStructureResponseDTO dto = new FeeStructureResponseDTO();
        dto.setStructureId(structure.getId()); // Note: getId() is from AuditableEntity
        dto.setName(structure.getName());
        dto.setAcademicYear(structure.getAcademicYear());
        dto.setDescription(structure.getDescription());
        dto.setActive(structure.isActive());
        dto.setCreatedAt(structure.getCreatedAt());
        dto.setUpdatedAt(structure.getUpdatedAt());

        // Map the list of particular entities to a list of particular DTOs
        dto.setParticulars(
                particulars.stream()
                        .map(this::toDto)
                        .collect(Collectors.toList())
        );
        return dto;
    }

    /**
     * Converts a single FeeParticular entity to its Response DTO.
     * This is a private helper for the main mapping method.
     */
    private FeeParticularResponseDTO toDto(FeeParticular particular) {
        FeeParticularResponseDTO dto = new FeeParticularResponseDTO();
        dto.setParticularId(particular.getParticularId());
        dto.setName(particular.getName());
        dto.setAmount(particular.getAmount());
        dto.setFrequency(particular.getFrequency());

        // Handle the FeeType relationship
        if (particular.getFeeType() != null) {
            dto.setFeeTypeId(particular.getFeeType().getFeeTypeId());
        }
        return dto;
    }
}