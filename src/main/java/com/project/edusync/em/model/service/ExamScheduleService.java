package com.project.edusync.em.model.service;

import com.project.edusync.em.model.dto.RequestDTO.ExamScheduleRequestDTO;
import com.project.edusync.em.model.dto.ResponseDTO.EvaluationStructureResponseDTO;
import com.project.edusync.em.model.dto.ResponseDTO.ExamScheduleResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ExamScheduleService {

    /**
     * Creates a schedule entry for a specific exam.
     */
    ExamScheduleResponseDTO createSchedule(UUID examUuid, ExamScheduleRequestDTO requestDTO);

    /**
     * Updates an existing schedule.
     */
    ExamScheduleResponseDTO updateSchedule(Long scheduleId, ExamScheduleRequestDTO requestDTO);

    /**
     * Fetches all schedules for a given exam.
     */
    List<ExamScheduleResponseDTO> getSchedulesByExam(UUID examUuid);

    /**
     * Fetches a single schedule by its ID.
     */
    ExamScheduleResponseDTO getScheduleById(Long scheduleId);

    /**
     * Deletes a schedule.
     */
    void deleteSchedule(Long scheduleId);

    /**
     * Builds question-wise evaluation structure from frozen schedule snapshot.
     */
    EvaluationStructureResponseDTO getEvaluationStructure(Long scheduleId);
}