package com.project.edusync.em.model.entity;

import com.project.edusync.common.model.AuditableEntity;
import com.project.edusync.em.model.enums.AnswerSheetStatus;
import com.project.edusync.uis.model.entity.Staff;
import com.project.edusync.uis.model.entity.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "answer_sheets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "schedule_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AttributeOverride(name = "id", column = @Column(name = "answer_sheet_id"))
public class AnswerSheet extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ExamSchedule examSchedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by_teacher_id", nullable = false)
    private Staff uploadedByTeacher;

    @Column(name = "file_url", length = 1000)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnswerSheetStatus status = AnswerSheetStatus.UPLOADED;
}

