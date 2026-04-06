package com.project.edusync.ams.model.service;

import com.project.edusync.ams.model.dto.request.StaffAttendanceRequestDTO;
import com.project.edusync.ams.model.dto.response.StaffAttendanceResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffAttendanceService {

    StaffAttendanceResponseDTO createAttendance(StaffAttendanceRequestDTO request, Long performedBy);

    List<StaffAttendanceResponseDTO> bulkCreate(List<StaffAttendanceRequestDTO> requests, Long performedBy);

    Page<StaffAttendanceResponseDTO> listAttendances(Pageable pageable,
                                                     Optional<UUID> staffUuid,
                                                     Optional<LocalDate> date);

    StaffAttendanceResponseDTO getAttendance(UUID recordUuid);

    StaffAttendanceResponseDTO updateAttendance(UUID recordUuid, StaffAttendanceRequestDTO request, Long performedBy);

    void deleteAttendance(UUID recordUuid, Long performedBy);
}
