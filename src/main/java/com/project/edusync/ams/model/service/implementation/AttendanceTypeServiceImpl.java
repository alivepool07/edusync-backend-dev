package com.project.edusync.ams.model.service.implementation;

import com.project.edusync.ams.model.dto.request.AttendanceTypeRequestDTO;
import com.project.edusync.ams.model.dto.response.AttendanceTypeResponseDTO;
import com.project.edusync.ams.model.entity.AttendanceType;
import com.project.edusync.ams.model.exception.AttendanceTypeInUseException;
import com.project.edusync.ams.model.exception.AttendanceTypeNotFoundException;
import com.project.edusync.ams.model.service.AttendanceTypeService; // Import the interface
import com.project.edusync.ams.model.repository.AttendanceTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceTypeServiceImpl implements AttendanceTypeService {

    private final AttendanceTypeRepository attendanceTypeRepository;

    // --- Private Mapping Methods for Clean Code ---

    private AttendanceType mapToEntity(AttendanceTypeRequestDTO dto) {
        AttendanceType entity = new AttendanceType();
        entity.setTypeName(dto.getTypeName());
        entity.setPresentMark(dto.getIsPresentMark());
        entity.setAbsenceMark(dto.getIsAbsenceMark());
        entity.setLateMark(dto.getIsLateMark());
        entity.setColorCode(dto.getColorCode());
        entity.setShortCode(dto.getShortCode());
        entity.setActive(true);
        return entity;
    }

    private AttendanceTypeResponseDTO mapToDto(AttendanceType entity) {
        return new AttendanceTypeResponseDTO(
                entity.getId(),
                entity.getUuid(),
                entity.getTypeName(),
                entity.getShortCode(),
                entity.isPresentMark(),
                entity.isAbsenceMark(),
                entity.isLateMark(),
                entity.getColorCode()
        );
    }

    // --- Interface Implementation ---

    @Override
    @Transactional
    public AttendanceTypeResponseDTO create(AttendanceTypeRequestDTO requestDTO) {
        log.info("AMS: Creating new attendance type: {}", requestDTO.getTypeName());
        AttendanceType newType = mapToEntity(requestDTO);
        AttendanceType savedType = attendanceTypeRepository.save(newType);
        return mapToDto(savedType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceTypeResponseDTO> findAllActive() {
        return attendanceTypeRepository.findByIsActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceTypeResponseDTO findByUuid(UUID typeUuid) {
        AttendanceType type = attendanceTypeRepository.findByUuidAndIsActiveTrue(typeUuid)
                .orElseThrow(() -> new AttendanceTypeNotFoundException(typeUuid.toString()));
        return mapToDto(type);
    }

    @Override
    @Transactional
    public AttendanceTypeResponseDTO update(UUID typeUuid, AttendanceTypeRequestDTO requestDTO) {
        log.info("AMS: Updating attendance type UUID: {}", typeUuid);
        AttendanceType typeToUpdate = attendanceTypeRepository.findByUuidAndIsActiveTrue(typeUuid)
                .orElseThrow(() -> new AttendanceTypeNotFoundException(typeUuid.toString()));

        // Update fields from DTO
        typeToUpdate.setTypeName(requestDTO.getTypeName());
        typeToUpdate.setShortCode(requestDTO.getShortCode());
        typeToUpdate.setPresentMark(requestDTO.getIsPresentMark());
        typeToUpdate.setAbsenceMark(requestDTO.getIsAbsenceMark());
        typeToUpdate.setLateMark(requestDTO.getIsLateMark());
        typeToUpdate.setColorCode(requestDTO.getColorCode());

        // AuditableEntity fields (updatedAt, updatedBy) are automatically managed.
        AttendanceType savedType = attendanceTypeRepository.save(typeToUpdate);
        return mapToDto(savedType);
    }

    @Override
    @Transactional
    public void softDelete(UUID typeUuid) {
        log.warn("AMS: Attempting soft delete (deactivation) of Attendance Type UUID: {}", typeUuid);
        AttendanceType typeToDelete = attendanceTypeRepository.findByUuidAndIsActiveTrue(typeUuid)
                .orElseThrow(() -> new AttendanceTypeNotFoundException(typeUuid.toString()));

        // --- PROFESSIONAL DATA INTEGRITY CHECK (Future Implementation) ---
        // Before soft-deleting, check if any StudentDailyAttendance or StaffDailyAttendance records
        // still reference this ID. If so, throw AttendanceTypeInUseException.
        // This ensures historical attendance data remains valid.

        typeToDelete.setActive(false);
        attendanceTypeRepository.save(typeToDelete);
        log.info("AMS: Successfully deactivated Attendance Type UUID: {}", typeUuid);
    }
}