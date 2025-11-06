package com.project.edusync.em.model.service;

import com.project.edusync.em.model.dto.RequestDTO.QuestionPaperRequestDTO;
import com.project.edusync.em.model.dto.ResponseDTO.QuestionPaperResponseDTO;

import java.util.UUID;

public interface QuestionPaperService {

    /**
     * Creates a new question paper and links it to all selected questions.
     * Validates total marks against the exam schedule.
     */
    QuestionPaperResponseDTO generateQuestionPaper(QuestionPaperRequestDTO requestDTO);

    /**
     * Retrieves a full question paper by its UUID.
     */
    QuestionPaperResponseDTO getQuestionPaperByUuid(UUID uuid);

    /**
     * Retrieves the question paper for a specific schedule ID.
     */
    QuestionPaperResponseDTO getQuestionPaperByScheduleId(Long scheduleId);

    /**
     * Deletes a question paper.
     */
    void deleteQuestionPaper(UUID uuid);
}