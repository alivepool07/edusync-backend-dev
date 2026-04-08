package com.project.edusync.em.model.dto.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamTemplateResponseDTO {
    private UUID id;
    private String name;
    private Integer totalMarks;
    private Integer totalQuestions;
    private boolean inUse;
    private LocalDateTime createdAt;
    private List<TemplateSectionResponseDTO> sections;
}

