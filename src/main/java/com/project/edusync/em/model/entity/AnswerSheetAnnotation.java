package com.project.edusync.em.model.entity;

import com.project.edusync.common.model.AuditableEntity;
import com.project.edusync.em.model.enums.AnnotationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "answer_sheet_annotations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AttributeOverride(name = "id", column = @Column(name = "annotation_id"))
public class AnswerSheetAnnotation extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "answer_sheet_id", nullable = false)
    private AnswerSheet answerSheet;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(nullable = false)
    private Double x;

    @Column(nullable = false)
    private Double y;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AnnotationType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
}

