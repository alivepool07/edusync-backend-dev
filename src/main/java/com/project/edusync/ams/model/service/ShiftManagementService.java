package com.project.edusync.ams.model.service;

import com.project.edusync.ams.model.dto.request.BulkStaffShiftMapRequestDTO;
import com.project.edusync.ams.model.dto.request.ShiftCreateDTO;
import com.project.edusync.ams.model.dto.request.StaffShiftMapRequestDTO;
import com.project.edusync.ams.model.dto.response.ShiftMappingResultDTO;
import com.project.edusync.ams.model.dto.response.ShiftResponseDTO;
import com.project.edusync.ams.model.dto.response.StaffShiftMappingResponseDTO;
import com.project.edusync.uis.model.enums.StaffCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ShiftManagementService {
    List<ShiftResponseDTO> listShifts();
    ShiftResponseDTO createShift(ShiftCreateDTO request);
    ShiftResponseDTO getShift(UUID shiftUuid);
    ShiftResponseDTO updateShift(UUID shiftUuid, ShiftCreateDTO request);
    void deleteShift(UUID shiftUuid);

    ShiftMappingResultDTO mapSingle(StaffShiftMapRequestDTO request);
    ShiftMappingResultDTO mapBulk(BulkStaffShiftMapRequestDTO request);
    void deleteMapping(UUID mappingUuid);

    Page<StaffShiftMappingResponseDTO> listMappings(Pageable pageable, UUID shiftUuid, StaffCategory category);
    StaffShiftMappingResponseDTO getCurrentMappingByStaff(UUID staffUuid);
}

