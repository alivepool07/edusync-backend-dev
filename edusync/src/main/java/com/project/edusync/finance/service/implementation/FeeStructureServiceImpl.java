package com.project.edusync.finance.service.implementation;

import com.project.edusync.finance.dto.feestructure.FeeParticularCreateDTO;
import com.project.edusync.finance.dto.feestructure.FeeStructureCreateDTO;
import com.project.edusync.finance.dto.feestructure.FeeStructureResponseDTO;
import com.project.edusync.finance.exception.FeeTypeNotFoundException;
import com.project.edusync.finance.mapper.FeeStructureMapper;
import com.project.edusync.finance.model.entity.FeeParticular;
import com.project.edusync.finance.model.entity.FeeStructure;
import com.project.edusync.finance.model.entity.FeeType;
import com.project.edusync.finance.repository.FeeParticularRepository;
import com.project.edusync.finance.repository.FeeStructureRepository;
import com.project.edusync.finance.repository.FeeTypeRepository;
import com.project.edusync.finance.service.FeeStructureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeeStructureServiceImpl implements FeeStructureService {

    // Inject all required repositories and the mapper
    private final FeeStructureRepository feeStructureRepository;
    private final FeeParticularRepository feeParticularRepository;
    private final FeeTypeRepository feeTypeRepository;
    private final FeeStructureMapper feeStructureMapper;

    @Override
    @Transactional
    public FeeStructureResponseDTO createFeeStructure(FeeStructureCreateDTO createDTO) {

        // 1. Map and save the parent FeeStructure
        FeeStructure feeStructure = new FeeStructure();
        feeStructure.setName(createDTO.getName());
        feeStructure.setAcademicYear(createDTO.getAcademicYear());
        feeStructure.setDescription(createDTO.getDescription());
        feeStructure.setActive(createDTO.isActive());

        FeeStructure savedStructure = feeStructureRepository.save(feeStructure);

        List<FeeParticular> savedParticulars = new ArrayList<>();

        // 2. Iterate and save each child FeeParticular
        if (createDTO.getParticulars() != null && !createDTO.getParticulars().isEmpty()) {
            for (FeeParticularCreateDTO particularDTO : createDTO.getParticulars()) {
                Long feeTypeId = particularDTO.getFeeTypeId();
                // 2a. Find the referenced FeeType
                FeeType feeType = feeTypeRepository.findById(particularDTO.getFeeTypeId())
                        .orElseThrow(() -> new FeeTypeNotFoundException("FeeType not found with id: " + feeTypeId));

                // 2b. Map DTO to entity
                FeeParticular particular = new FeeParticular();
                particular.setName(particularDTO.getName());
                particular.setAmount(particularDTO.getAmount());
                particular.setFrequency(particularDTO.getFrequency());

                // 2c. Set relationships
                particular.setFeeType(feeType);
                particular.setFeeStructure(savedStructure); // Link to parent

                // 2d. Save the particular
                savedParticulars.add(feeParticularRepository.save(particular));
            }
        }

        // 3. Map the saved entities to the response DTO
        return feeStructureMapper.toDto(savedStructure, savedParticulars);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeeStructureResponseDTO> getAllFeeStructures() {

        List<FeeStructure> structures = feeStructureRepository.findAll();

        return structures.stream()
                .map(structure -> {
                    // For each structure, find its particulars
                    List<FeeParticular> particulars = feeParticularRepository.findByFeeStructure_Id(structure.getId());
                    // Map to the response DTO
                    return feeStructureMapper.toDto(structure, particulars);
                })
                .collect(Collectors.toList());
    }
}