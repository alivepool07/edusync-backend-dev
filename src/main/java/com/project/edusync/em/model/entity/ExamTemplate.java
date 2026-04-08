package com.project.edusync.em.model.entity;

import com.project.edusync.common.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exam_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AttributeOverride(name = "id", column = @Column(name = "template_id"))
public class ExamTemplate extends AuditableEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "in_use", nullable = false)
    private boolean inUse;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.project.edusync.em.model.entity.TemplateSection> sections = new ArrayList<>();
}

