package com.project.edusync.em.model.entity;

import com.project.edusync.common.model.AuditableEntity;
import com.project.edusync.em.model.enums.EvaluationAuditEventType;
import com.project.edusync.uis.model.entity.Staff;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "evaluation_audit_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AttributeOverride(name = "id", column = @Column(name = "audit_event_id"))
public class EvaluationAuditEvent extends AuditableEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EvaluationAuditEventType eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_teacher_id")
    private Staff actorTeacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private EvaluationAssignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_sheet_id")
    private AnswerSheet answerSheet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_result_id")
    private EvaluationResult evaluationResult;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_metadata", columnDefinition = "jsonb")
    private Map<String, Object> eventMetadata;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}

