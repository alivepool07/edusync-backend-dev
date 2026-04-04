package com.project.edusync.hrms.service;

import com.project.edusync.hrms.dto.leave.LeaveTypeConfigCreateDTO;
import com.project.edusync.hrms.dto.leave.LeaveTypeConfigResponseDTO;
import com.project.edusync.hrms.dto.leave.LeaveTypeConfigUpdateDTO;

import java.util.List;

public interface LeaveTypeConfigService {

    List<LeaveTypeConfigResponseDTO> getAll();

    LeaveTypeConfigResponseDTO getById(Long leaveTypeId);

    LeaveTypeConfigResponseDTO create(LeaveTypeConfigCreateDTO dto);

    LeaveTypeConfigResponseDTO update(Long leaveTypeId, LeaveTypeConfigUpdateDTO dto);

    void delete(Long leaveTypeId);
}

