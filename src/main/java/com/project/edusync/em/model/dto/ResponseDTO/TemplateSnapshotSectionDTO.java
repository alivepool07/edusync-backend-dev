package com.project.edusync.em.model.dto.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSnapshotSectionDTO {
    private String name;
    private Integer sectionOrder;
    private Integer questionCount;
    private Integer marksPerQuestion;
}

