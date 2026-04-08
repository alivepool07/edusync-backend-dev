package com.project.edusync.em.model.entity.snapshot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSnapshot {
    private String templateName;
    private Integer totalMarks;
    private Integer totalQuestions;

    @Builder.Default
    private List<com.project.edusync.em.model.entity.snapshot.TemplateSnapshotSection> sections = new ArrayList<>();
}

