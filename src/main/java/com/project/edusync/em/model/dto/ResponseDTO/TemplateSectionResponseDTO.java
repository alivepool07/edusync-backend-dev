package com.project.edusync.em.model.dto.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSectionResponseDTO {
    private UUID id;
    private String sectionName;
    private Integer sectionOrder;
    private Integer questionCount;
    private Integer marksPerQuestion;
    private Boolean isObjective;
    private Boolean isSubjective;
}

