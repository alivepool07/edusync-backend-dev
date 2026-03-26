package com.project.edusync.adm.service.impl;

import com.project.edusync.adm.exception.DuplicateEntryException;
import com.project.edusync.adm.exception.ResourceNotFoundException;
import com.project.edusync.adm.model.dto.request.SubjectRequestDto;
import com.project.edusync.adm.model.dto.response.SubjectResponseDto;
import com.project.edusync.adm.model.entity.Subject;
import com.project.edusync.adm.repository.SubjectRepository;
import com.project.edusync.adm.service.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;

    @Override
    @CacheEvict(value = {"allSubjects", "editorContext"}, allEntries = true)
    public SubjectResponseDto addSubject(SubjectRequestDto subjectRequestDto) {
        log.info("Attempting to create a new subject with code: {}", subjectRequestDto.getSubjectCode());

        // Best Practice: Validate for uniqueness
        if (subjectRepository.existsBySubjectCode(subjectRequestDto.getSubjectCode())) {
            log.warn("Subject creation failed. Code '{}' already exists.", subjectRequestDto.getSubjectCode());
            throw new DuplicateEntryException("Subject with code " + subjectRequestDto.getSubjectCode() + " already exists.");
        }

        Subject newSubject = new Subject();
        newSubject.setName(subjectRequestDto.getName());
        newSubject.setSubjectCode(subjectRequestDto.getSubjectCode());
        newSubject.setRequiresSpecialRoomType(subjectRequestDto.getRequiresSpecialRoomType());
        newSubject.setColor(subjectRequestDto.getColor());
        newSubject.setIsActive(true); // Explicitly set as active

        Subject savedSubject = subjectRepository.save(newSubject);
        log.info("Subject '{}' created successfully with id {}", savedSubject.getName(), savedSubject.getUuid());

        return toSubjectResponseDto(savedSubject);
    }

    @Override
    @Cacheable(value = "allSubjects", key = "'ALL'")
    public List<SubjectResponseDto> getAllSubjects() {
        log.info("Fetching all active subjects");
        return subjectRepository.findAll().stream()
                .filter(Subject::getIsActive) // Only return active subjects
                .map(this::toSubjectResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public SubjectResponseDto getSubjectById(UUID subjectId) {
        log.info("Fetching subject with id: {}", subjectId);
        Subject subject = subjectRepository.findActiveById(subjectId)
                .orElseThrow(() -> {
                    log.warn("No active subject with id {} found", subjectId);
                    return new ResourceNotFoundException("No resource found with id: " + subjectId);
                });
        return toSubjectResponseDto(subject);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"allSubjects", "editorContext"}, allEntries = true)
    public SubjectResponseDto updateSubject(UUID subjectId, SubjectRequestDto subjectRequestDto) {
        log.info("Attempting to update subject with id: {}", subjectId);
        Subject existingSubject = subjectRepository.findActiveById(subjectId)
                .orElseThrow(() -> {
                    log.warn("No active subject with id {} to update", subjectId);
                    return new ResourceNotFoundException("No resource found to update with id: " + subjectId);
                });

        // Check uniqueness of subject code only if it's being changed
        if (!existingSubject.getSubjectCode().equals(subjectRequestDto.getSubjectCode())) {
            if (subjectRepository.existsBySubjectCodeAndUuidNot(subjectRequestDto.getSubjectCode(), subjectId)) {
                log.warn("Subject update failed. Code '{}' already exists for another subject.", subjectRequestDto.getSubjectCode());
                throw new DuplicateEntryException("Subject with code " + subjectRequestDto.getSubjectCode() + " already exists.");
            }
        }

        existingSubject.setName(subjectRequestDto.getName());
        existingSubject.setSubjectCode(subjectRequestDto.getSubjectCode());
        existingSubject.setRequiresSpecialRoomType(subjectRequestDto.getRequiresSpecialRoomType());
        existingSubject.setColor(subjectRequestDto.getColor());

        Subject updatedSubject = subjectRepository.save(existingSubject);
        log.info("Subject with id {} updated successfully", updatedSubject.getUuid());

        return toSubjectResponseDto(updatedSubject);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"allSubjects", "editorContext"}, allEntries = true)
    public void deleteSubject(UUID subjectId) {
        log.info("Attempting to soft delete subject with id: {}", subjectId);
        if (!subjectRepository.existsActiveById(subjectId)) {
            log.warn("Failed to delete. Subject not found with id: {}", subjectId);
            throw new ResourceNotFoundException("Subject id: " + subjectId + " not found.");
        }

        subjectRepository.softDeleteById(subjectId);
        log.info("Subject with id {} marked as inactive successfully", subjectId);
    }

    /**
     * Private helper to convert Subject Entity to Response DTO using builder.
     */
    private SubjectResponseDto toSubjectResponseDto(Subject entity) {
        if (entity == null) return null;
        return SubjectResponseDto.builder()
                .uuid(entity.getUuid())
                .name(entity.getName())
                .subjectCode(entity.getSubjectCode())
                .requiresSpecialRoomType(entity.getRequiresSpecialRoomType())
                .color(entity.getColor())
                .build();
    }
}