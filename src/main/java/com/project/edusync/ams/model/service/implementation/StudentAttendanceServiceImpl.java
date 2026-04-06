package com.project.edusync.ams.model.service.implementation;

import com.project.edusync.ams.model.dto.request.StudentAttendanceRequestDTO;
import com.project.edusync.ams.model.dto.response.AttendanceTypeResponseDTO;
import com.project.edusync.ams.model.dto.response.AbsenceDocumentationSummaryResponseDTO;
import com.project.edusync.ams.model.dto.response.StudentAttendanceResponseDTO;
import com.project.edusync.ams.model.entity.AttendanceType;
import com.project.edusync.ams.model.entity.AbsenceDocumentation;
import com.project.edusync.ams.model.entity.StudentDailyAttendance;
import com.project.edusync.ams.model.exception.AttendanceProcessingException;
import com.project.edusync.ams.model.exception.AttendanceRecordNotFoundException;
import com.project.edusync.ams.model.exception.InvalidAttendanceTypeException;
import com.project.edusync.ams.model.repository.AttendanceTypeRepository;
import com.project.edusync.ams.model.repository.StudentDailyAttendanceRepository;
import com.project.edusync.ams.model.repository.AbsenceDocumentationRepository;
import com.project.edusync.ams.model.service.StudentAttendanceService;
import com.project.edusync.uis.repository.StaffRepository;
import com.project.edusync.uis.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * StudentAttendanceServiceImpl — implements StudentAttendanceService
 * Uses AttendanceType entity relation on StudentDailyAttendance (setAttendanceType / getAttendanceType).
 *
 * Conservative implementation: avoids calling non-existent getters/setters
 * and maps types to match DTO constructors exactly.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentAttendanceServiceImpl implements StudentAttendanceService {

    private final StudentDailyAttendanceRepository studentRepo;
    private final AttendanceTypeRepository attendanceTypeRepository;
    private final AbsenceDocumentationRepository absenceDocumentationRepository;
    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;

    @Override
    @Transactional
    @CacheEvict(value = {"dashboard", "dashboardOverview"}, allEntries = true)
    public List<StudentAttendanceResponseDTO> markAttendanceBatch(List<StudentAttendanceRequestDTO> requests, Long performedByStaffId) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        // Collect unique uppercased short codes
        Set<String> shortCodes = requests.stream()
                .map(r -> Optional.ofNullable(r.getAttendanceShortCode()).orElse("").trim().toUpperCase())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        // Validate existence of short codes by loading AttendanceType entities
        Map<String, AttendanceType> shortCodeToType = new HashMap<>();
        if (!shortCodes.isEmpty()) {
            for (String sc : shortCodes) {
                attendanceTypeRepository.findByShortCodeIgnoreCase(sc)
                        .ifPresent(t -> shortCodeToType.put(sc, t));
            }
            // report missing
            Set<String> missing = new HashSet<>(shortCodes);
            missing.removeAll(shortCodeToType.keySet());
            if (!missing.isEmpty()) {
                log.warn("Attendance short codes not found: {}", missing);
                throw new InvalidAttendanceTypeException("Attendance short code(s) not found: " + missing);
            }
        }

        List<StudentDailyAttendance> savedEntities = new ArrayList<>(requests.size());

        for (StudentAttendanceRequestDTO req : requests) {
            // Row-level validation
            Long resolvedStudentId = resolveStudentId(req);
            if (req.getAttendanceDate() == null) {
                throw new AttendanceProcessingException("attendanceDate is required for each attendance record");
            }
            String sc = Optional.ofNullable(req.getAttendanceShortCode()).orElse("").trim().toUpperCase();
            if (sc.isEmpty()) {
                throw new InvalidAttendanceTypeException("attendanceShortCode is required (e.g., P, A, L)");
            }

            AttendanceType attendanceType = shortCodeToType.get(sc);
            if (attendanceType == null) {
                // Defensive: try a direct lookup
                attendanceType = attendanceTypeRepository.findByShortCodeIgnoreCase(sc)
                        .orElseThrow(() -> new InvalidAttendanceTypeException("Attendance short code not found: " + sc));
            }

            // Upsert by unique (studentId + date)
            Optional<StudentDailyAttendance> existingOpt = studentRepo.findByStudentIdAndAttendanceDate(resolvedStudentId, req.getAttendanceDate());
            StudentDailyAttendance entity;
            if (existingOpt.isPresent()) {
                entity = existingOpt.get();

                // If same attendance type and same notes, skip update to avoid churn
                AttendanceType currentType = entity.getAttendanceType();
                boolean sameType = currentType != null && Objects.equals(currentType.getId(), attendanceType.getId());
                boolean sameNotes = Objects.equals(entity.getNotes(), req.getNotes());
                if (sameType && sameNotes) {
                    savedEntities.add(entity);
                    continue;
                }
            } else {
                entity = new StudentDailyAttendance();
                entity.setStudentId(resolvedStudentId);
                entity.setAttendanceDate(req.getAttendanceDate());
            }

            // Set relationship to AttendanceType entity
            entity.setAttendanceType(attendanceType);

            // TakenBy staff id - use performedByStaffId if present, else DTO's takenBy
            entity.setTakenByStaffId(Optional.ofNullable(performedByStaffId).orElseGet(() -> resolveTakenByStaffId(req)));
            entity.setNotes(req.getNotes());

            StudentDailyAttendance saved = studentRepo.save(entity);
            savedEntities.add(saved);
        }

        // Map to response DTOs
        return savedEntities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<StudentAttendanceResponseDTO> listAttendances(Pageable pageable,
                                                              Optional<UUID> studentUuid,
                                                              Optional<UUID> takenByStaffUuid,
                                                              Optional<String> fromDateIso,
                                                              Optional<String> toDateIso,
                                                              Optional<String> attendanceTypeShortCode) {

        Optional<Long> studentId = studentUuid.map(this::resolveStudentIdFromUuid);
        Optional<Long> takenByStaffId = takenByStaffUuid.map(this::resolveStaffIdFromUuid);

        // NOTE: For now we use a simple repo.findAll(pageable) and then do in-memory filters if any filter present.
        // For production: replace with Specification and studentRepo.findAll(spec, pageable).
        Page<StudentDailyAttendance> page = studentRepo.findAll(pageable);

        List<StudentDailyAttendance> content = page.getContent().stream()
                .filter(e -> studentId.map(id -> Objects.equals(e.getStudentId(), id)).orElse(true))
                .filter(e -> takenByStaffId.map(id -> Objects.equals(e.getTakenByStaffId(), id)).orElse(true))
                .filter(e -> {
                    if (attendanceTypeShortCode.isPresent()) {
                        String sc = attendanceTypeShortCode.get().trim().toUpperCase();
                        AttendanceType at = e.getAttendanceType();
                        String got = at == null ? null : Optional.ofNullable(at.getShortCode()).orElse("").trim().toUpperCase();
                        return sc.equals(got);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        List<StudentAttendanceResponseDTO> dtoList = content.stream().map(this::toResponseDto).collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    @Override
    public StudentAttendanceResponseDTO getAttendance(UUID recordUuid) {
        StudentDailyAttendance e = studentRepo.findByUuid(recordUuid)
                .orElseThrow(() -> new AttendanceRecordNotFoundException("Attendance record not found with uuid: " + recordUuid));
        return toResponseDto(e);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"dashboard", "dashboardOverview"}, allEntries = true)
    public StudentAttendanceResponseDTO updateAttendance(UUID recordUuid, StudentAttendanceRequestDTO req, Long performedByStaffId) {
        StudentDailyAttendance existing = studentRepo.findByUuid(recordUuid)
                .orElseThrow(() -> new AttendanceRecordNotFoundException("Attendance record not found with uuid: " + recordUuid));

        // Do not allow attendanceDate change
        if (req.getAttendanceDate() != null && !req.getAttendanceDate().equals(existing.getAttendanceDate())) {
            throw new AttendanceProcessingException("attendanceDate cannot be changed for existing record");
        }

        if (req.getAttendanceShortCode() != null) {
            String sc = req.getAttendanceShortCode().trim().toUpperCase();
            AttendanceType t = attendanceTypeRepository.findByShortCodeIgnoreCase(sc)
                    .orElseThrow(() -> new InvalidAttendanceTypeException("Attendance short code not found: " + sc));
            existing.setAttendanceType(t);
        }

        if (req.getNotes() != null) existing.setNotes(req.getNotes());
        if (performedByStaffId != null) existing.setTakenByStaffId(performedByStaffId);

        StudentDailyAttendance saved = studentRepo.save(existing);
        return toResponseDto(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"dashboard", "dashboardOverview"}, allEntries = true)
    public void deleteAttendance(UUID recordUuid, Long performedByStaffId) {
        StudentDailyAttendance existing = studentRepo.findByUuid(recordUuid)
                .orElseThrow(() -> new AttendanceRecordNotFoundException("Attendance record not found with uuid: " + recordUuid));

        // Soft-delete if entity has 'setDeleted' method (some entities in codebase use AuditableEntity)
        try {
            existing.getClass().getDeclaredMethod("setDeleted", Boolean.class);
            existing.getClass().getMethod("setDeleted", Boolean.class).invoke(existing, Boolean.TRUE);
            try {
                existing.getClass().getMethod("setDeletedAt", LocalDateTime.class).invoke(existing, LocalDateTime.now());
            } catch (NoSuchMethodException ignore) {
            }
            studentRepo.save(existing);
        } catch (NoSuchMethodException e) {
            studentRepo.delete(existing);
        } catch (Exception ex) {
            throw new AttendanceProcessingException("Failed to delete attendance record: " + ex.getMessage());
        }
    }

    /* ------------------ Helper: map entity -> DTO ------------------ */

    private StudentAttendanceResponseDTO toResponseDto(StudentDailyAttendance e) {
        if (e == null) return null;

        AttendanceTypeResponseDTO typeDto = null;
        AttendanceType at = e.getAttendanceType();
        if (at != null) {
            typeDto = new AttendanceTypeResponseDTO(
                    at.getId(),
                    at.getUuid(),
                    at.getTypeName(),
                    at.getShortCode(),
                    at.isPresentMark(),
                    at.isAbsenceMark(),
                    at.isLateMark(),
                    at.getColorCode()
            );
        }

        AbsenceDocumentationSummaryResponseDTO absenceSummary = null;
        AbsenceDocumentation ad = e.getAbsenceDocumentation();
        if (ad != null) {
            absenceSummary = new AbsenceDocumentationSummaryResponseDTO(
                    ad.getId(),
                    ad.getApprovalStatus(),
                    ad.getDocumentationUrl()
            );
        }

        // studentFullName and takenByStaffName are not part of the entity and require external lookup (UIS).
        String studentFullName = null;
        String takenByStaffName = null;

        String studentUuid = null;
        if (e.getStudentId() != null) {
            studentUuid = studentRepository.findById(e.getStudentId())
                    .map(s -> s.getUuid() == null ? null : s.getUuid().toString())
                    .orElse(null);
        }

        String takenByStaffUuid = null;
        if (e.getTakenByStaffId() != null) {
            takenByStaffUuid = staffRepository.findById(e.getTakenByStaffId())
                    .map(s -> s.getUuid() == null ? null : s.getUuid().toString())
                    .orElse(null);
        }

        // IMPORTANT: convert UUID to String to match DTO constructor signature
        String uuidStr = null;
        if (e.getUuid() != null) {
            uuidStr = e.getUuid().toString();
        }

        return new StudentAttendanceResponseDTO(
                e.getId(),                // Long dailyAttendanceId
                uuidStr,                  // String uuid  <- convert UUID -> String
                studentUuid,
                e.getStudentId(),         // Long studentId
                studentFullName,          // String studentFullName
                e.getAttendanceDate(),    // LocalDate attendanceDate
                at == null ? null : at.getShortCode(), // String attendanceTypeShortCode
                takenByStaffUuid,
                e.getTakenByStaffId(),    // Long takenByStaffId
                takenByStaffName,         // String takenByStaffName
                typeDto,                  // AttendanceTypeResponseDTO attendanceType
                e.getNotes(),             // String notes
                absenceSummary,           // AbsenceDocumentationSummaryResponseDTO absenceDocumentation
                e.getCreatedAt(),         // LocalDateTime createdAt
                e.getCreatedBy()          // String createdBy
        );
    }

    private Long resolveStudentId(StudentAttendanceRequestDTO req) {
        if (req.getStudentUuid() != null) {
            return resolveStudentIdFromUuid(req.getStudentUuid());
        }
        if (req.getStudentId() != null) {
            return req.getStudentId();
        }
        throw new AttendanceProcessingException("studentUuid is required (or deprecated studentId during transition)");
    }

    private Long resolveTakenByStaffId(StudentAttendanceRequestDTO req) {
        if (req.getTakenByStaffUuid() != null) {
            return resolveStaffIdFromUuid(req.getTakenByStaffUuid());
        }
        if (req.getTakenByStaffId() != null) {
            return req.getTakenByStaffId();
        }
        throw new AttendanceProcessingException("takenByStaffUuid is required (or deprecated takenByStaffId during transition)");
    }

    private Long resolveStudentIdFromUuid(UUID studentUuid) {
        return studentRepository.findByUuid(studentUuid)
                .map(s -> s.getId())
                .orElseThrow(() -> new AttendanceProcessingException("Student not found for uuid: " + studentUuid));
    }

    private Long resolveStaffIdFromUuid(UUID staffUuid) {
        return staffRepository.findByUuid(staffUuid)
                .map(s -> s.getId())
                .orElseThrow(() -> new AttendanceProcessingException("Staff not found for uuid: " + staffUuid));
    }
}
