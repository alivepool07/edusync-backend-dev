package com.project.edusync.em.model.service;

import com.project.edusync.em.model.dto.RequestDTO.BulkMarkRequestDTO;
import com.project.edusync.em.model.dto.RequestDTO.StudentMarkRequestDTO;
import com.project.edusync.em.model.dto.ResponseDTO.StudentMarkResponseDTO;

import java.util.List;
import java.util.UUID;

public interface StudentMarkService {

    /**
     * Records or updates marks for multiple students for a specific exam schedule.
     * This acts as an "Upsert" (Update or Insert) operation.
     */
    List<StudentMarkResponseDTO> recordBulkMarks(Long scheduleId, BulkMarkRequestDTO bulkRequest);

    /**
     * Updates a single mark entry (e.g., for a correction after initial bulk entry).
     */
    StudentMarkResponseDTO updateMark(UUID markUuid, StudentMarkRequestDTO requestDTO);

    /**
     * Retrieves all marks entered for a specific exam schedule (Teacher Gradebook View).
     */
    List<StudentMarkResponseDTO> getMarksBySchedule(Long scheduleId);

    /**
     * Retrieves mark history for a specific student (Student Portal View).
     */
    // Note: Implementing this would require a custom repository method findByStudentUuid
    // List<StudentMarkResponseDTO> getMarksByStudent(UUID studentUuid);
}