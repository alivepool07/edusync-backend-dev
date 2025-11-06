package com.project.edusync.em.model.service;

import com.project.edusync.em.model.dto.RequestDTO.GradeSystemRequestDTO;
import com.project.edusync.em.model.dto.ResponseDTO.GradeSystemResponseDTO;

import java.util.List;
import java.util.UUID;

public interface GradeSystemService {

    /**
     * Creates a new grading system with all its scale rules.
     */
    GradeSystemResponseDTO createGradeSystem(GradeSystemRequestDTO requestDTO);

    /**
     * Updates an existing grading system.
     * This will replace all old scales with the new ones provided.
     */
    GradeSystemResponseDTO updateGradeSystem(UUID uuid, GradeSystemRequestDTO requestDTO);

    /**
     * Fetches a specific grade system by UUID.
     */
    GradeSystemResponseDTO getGradeSystemByUuid(UUID uuid);

    /**
     * Fetches all active grading systems.
     */
    List<GradeSystemResponseDTO> getAllActiveGradeSystems();

    /**
     * Soft-deletes a grading system (sets isActive = false).
     */
    void deleteGradeSystem(UUID uuid);
}