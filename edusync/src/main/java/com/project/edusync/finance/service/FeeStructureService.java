package com.project.edusync.finance.service;

import com.project.edusync.finance.dto.feestructure.FeeStructureCreateDTO;
import com.project.edusync.finance.dto.feestructure.FeeStructureResponseDTO;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Service interface for managing Fee Structures.
 * Defines the contract for business logic operations.
 */
public interface FeeStructureService {

    /**
     * Creates a new FeeStructure and its associated FeeParticulars.
     *
     * @param createDTO The DTO containing data for the structure and its particulars.
     * @return The response DTO of the newly created structure.
     */
    @Transactional
    FeeStructureResponseDTO createFeeStructure(FeeStructureCreateDTO createDTO);

    /**
     * Retrieves all FeeStructures and their associated FeeParticulars.
     *
     * @return A list of FeeStructureResponseDTOs.
     */
    List<FeeStructureResponseDTO> getAllFeeStructures();

    // We will add getById, update, and delete methods here later.
}
