package com.project.edusync.em.model.dto.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSnapshotResponseDTO {
    private String templateName;
    private Integer totalMarks;
    private Integer totalQuestions;
    private List<TemplateSnapshotSectionDTO> sections;
}

