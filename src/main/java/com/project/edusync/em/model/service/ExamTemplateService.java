package com.project.edusync.em.model.service;

import com.project.edusync.em.model.dto.RequestDTO.ExamTemplateRequestDTO;
import com.project.edusync.em.model.dto.ResponseDTO.EvaluationStructureResponseDTO;
import com.project.edusync.em.model.dto.ResponseDTO.ExamTemplateResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ExamTemplateService {
    ExamTemplateResponseDTO createTemplate(ExamTemplateRequestDTO requestDTO);

    List<ExamTemplateResponseDTO> getAllTemplates();

    ExamTemplateResponseDTO getTemplateById(UUID templateId);

    ExamTemplateResponseDTO updateTemplate(UUID templateId, ExamTemplateRequestDTO requestDTO);

    void deleteTemplate(UUID templateId);

    EvaluationStructureResponseDTO getTemplatePreview(UUID templateId);
}

