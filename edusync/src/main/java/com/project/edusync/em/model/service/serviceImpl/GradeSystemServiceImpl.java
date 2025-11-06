package com.project.edusync.em.model.service.serviceImpl;

import com.project.edusync.common.exception.emException.EdusyncException;
import com.project.edusync.em.model.dto.RequestDTO.GradeScaleRequestDTO;
import com.project.edusync.em.model.dto.RequestDTO.GradeSystemRequestDTO;
import com.project.edusync.em.model.dto.ResponseDTO.GradeScaleResponseDTO;
import com.project.edusync.em.model.dto.ResponseDTO.GradeSystemResponseDTO;
import com.project.edusync.em.model.entity.GradeScale;
import com.project.edusync.em.model.entity.GradeSystem;
import com.project.edusync.em.model.repository.GradeSystemRepository;
import com.project.edusync.em.model.service.GradeSystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GradeSystemServiceImpl implements GradeSystemService {

    private final GradeSystemRepository gradeSystemRepository;

    @Override
    public GradeSystemResponseDTO createGradeSystem(GradeSystemRequestDTO requestDTO) {
        log.info("Creating new Grade System: {}", requestDTO.getSystemName());

        // 1. Validate for overlapping scales
        validateScaleOverlaps(requestDTO.getGradeScales());

        GradeSystem gradeSystem = new GradeSystem();
        gradeSystem.setSystemName(requestDTO.getSystemName());
        gradeSystem.setDescription(requestDTO.getDescription());
        gradeSystem.setActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : true);

        // 2. Map and set child entities (GradeScales)
        Set<GradeScale> scales = requestDTO.getGradeScales().stream()
                .map(dto -> mapScaleDtoToEntity(dto, gradeSystem))
                .collect(Collectors.toSet());
        gradeSystem.setGradeScales(scales);

        GradeSystem savedSystem = gradeSystemRepository.save(gradeSystem);
        log.info("Grade System created with UUID: {}", savedSystem.getUuid());

        return toResponseDTO(savedSystem);
    }

    @Override
    public GradeSystemResponseDTO updateGradeSystem(UUID uuid, GradeSystemRequestDTO requestDTO) {
        log.info("Updating Grade System with UUID: {}", uuid);

        GradeSystem existingSystem = gradeSystemRepository.findByUuid(uuid)
                .orElseThrow(() -> new EdusyncException("EM-404", "Grade System not found", HttpStatus.NOT_FOUND));

        validateScaleOverlaps(requestDTO.getGradeScales());

        existingSystem.setSystemName(requestDTO.getSystemName());
        existingSystem.setDescription(requestDTO.getDescription());
        if (requestDTO.getIsActive() != null) {
            existingSystem.setActive(requestDTO.getIsActive());
        }

        // 3. Full replacement of child collection (easiest way to handle updates/deletes of scales)
        existingSystem.getGradeScales().clear();
        Set<GradeScale> newScales = requestDTO.getGradeScales().stream()
                .map(dto -> mapScaleDtoToEntity(dto, existingSystem))
                .collect(Collectors.toSet());
        existingSystem.getGradeScales().addAll(newScales);

        GradeSystem updatedSystem = gradeSystemRepository.save(existingSystem);
        return toResponseDTO(updatedSystem);
    }

    @Override
    @Transactional(readOnly = true)
    public GradeSystemResponseDTO getGradeSystemByUuid(UUID uuid) {
        GradeSystem system = gradeSystemRepository.findByUuid(uuid)
                .orElseThrow(() -> new EdusyncException("EM-404", "Grade System not found", HttpStatus.NOT_FOUND));
        return toResponseDTO(system);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeSystemResponseDTO> getAllActiveGradeSystems() {
        // Assuming you might add a custom repository method: findAllByIsActiveTrue()
        // For now, filtering in stream if repository method doesn't exist.
        return gradeSystemRepository.findAll().stream()
                .filter(GradeSystem::isActive)
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteGradeSystem(UUID uuid) {
        log.info("Soft-deleting Grade System with UUID: {}", uuid);
        GradeSystem system = gradeSystemRepository.findByUuid(uuid)
                .orElseThrow(() -> new EdusyncException("EM-404", "Grade System not found", HttpStatus.NOT_FOUND));

        system.setActive(false);
        gradeSystemRepository.save(system);
    }

    // --- Helper Methods ---

    private void validateScaleOverlaps(Set<GradeScaleRequestDTO> scales) {
        // A simple O(N^2) check is fine as the number of scales is small (usually < 15)
        for (GradeScaleRequestDTO s1 : scales) {
            if (s1.getMinPercentage().compareTo(s1.getMaxPercentage()) > 0) {
                throw new EdusyncException("EM-400", "Min percentage cannot be greater than max percentage for grade: " + s1.getGradeName(), HttpStatus.BAD_REQUEST);
            }
            for (GradeScaleRequestDTO s2 : scales) {
                if (s1 == s2) continue;
                // Check for overlap: (StartA <= EndB) and (EndA >= StartB)
                if (s1.getMinPercentage().compareTo(s2.getMaxPercentage()) < 0 &&
                        s1.getMaxPercentage().compareTo(s2.getMinPercentage()) > 0) {
                    throw new EdusyncException("EM-400",
                            "Overlapping grade scales detected between " + s1.getGradeName() + " and " + s2.getGradeName(),
                            HttpStatus.BAD_REQUEST);
                }
            }
        }
    }

    private GradeScale mapScaleDtoToEntity(GradeScaleRequestDTO dto, GradeSystem parent) {
        GradeScale scale = new GradeScale();
        scale.setGradeName(dto.getGradeName());
        scale.setMinPercentage(dto.getMinPercentage());
        scale.setMaxPercentage(dto.getMaxPercentage());
        scale.setGradePoints(dto.getGradePoints());
        scale.setGradeSystem(parent); // Critical for JPA relationship
        return scale;
    }

    private GradeSystemResponseDTO toResponseDTO(GradeSystem entity) {
        return GradeSystemResponseDTO.builder()
                .uuid(entity.getUuid())
                .systemName(entity.getSystemName())
                .description(entity.getDescription())
                .isActive(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .gradeScales(entity.getGradeScales().stream()
                        .map(scale -> GradeScaleResponseDTO.builder()
                                .gradeScaleId(scale.getGradeScaleId())
                                .gradeName(scale.getGradeName())
                                .minPercentage(scale.getMinPercentage())
                                .maxPercentage(scale.getMaxPercentage())
                                .gradePoints(scale.getGradePoints())
                                .build())
                        .collect(Collectors.toSet()))
                .build();
    }
}