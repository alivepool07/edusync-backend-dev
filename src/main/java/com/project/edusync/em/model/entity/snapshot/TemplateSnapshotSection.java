package com.project.edusync.em.model.entity.snapshot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSnapshotSection {
    private String name;
    private Integer sectionOrder;
    private Integer questionCount;
    private Integer marksPerQuestion;
}

