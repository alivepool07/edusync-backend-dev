package com.project.edusync.em.model.entity;

import com.project.edusync.common.model.AuditableEntity;
import com.project.edusync.em.model.enums.EvaluationResultStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluation_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AttributeOverride(name = "id", column = @Column(name = "evaluation_result_id"))
public class EvaluationResult extends AuditableEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "answer_sheet_id", nullable = false, unique = true)
    private AnswerSheet answerSheet;

    @Column(name = "total_marks", nullable = false, precision = 7, scale = 2)
    private BigDecimal totalMarks = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EvaluationResultStatus status = EvaluationResultStatus.DRAFT;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;
}

