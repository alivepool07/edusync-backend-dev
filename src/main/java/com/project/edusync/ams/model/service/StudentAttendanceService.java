package com.project.edusync.ams.model.service;

import com.project.edusync.ams.model.dto.request.StudentAttendanceRequestDTO;
import com.project.edusync.ams.model.dto.response.StudentAttendanceResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentAttendanceService {

    List<StudentAttendanceResponseDTO> markAttendanceBatch(
            List<StudentAttendanceRequestDTO> requests,
            Long performedByStaffId
    );

    Page<StudentAttendanceResponseDTO> listAttendances(
            Pageable pageable,
            Optional<UUID> studentUuid,
            Optional<UUID> takenByStaffUuid,
            Optional<String> fromDateIso,
            Optional<String> toDateIso,
            Optional<String> attendanceTypeShortCode
    );

    StudentAttendanceResponseDTO getAttendance(UUID recordUuid);

    StudentAttendanceResponseDTO updateAttendance(
            UUID recordUuid,
            StudentAttendanceRequestDTO req,
            Long performedByStaffId
    );

    void deleteAttendance(UUID recordUuid, Long performedByStaffId);
}
