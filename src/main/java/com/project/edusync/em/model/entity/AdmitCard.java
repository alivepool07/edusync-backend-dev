package com.project.edusync.em.model.entity;

import com.project.edusync.common.model.AuditableEntity;
import com.project.edusync.em.model.enums.AdmitCardStatus;
import com.project.edusync.iam.model.entity.User;
import com.project.edusync.uis.model.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "admit_cards", uniqueConstraints = {
        @UniqueConstraint(name = "uk_admit_card_exam_student", columnNames = {"exam_id", "student_id"})
}, indexes = {
        @Index(name = "idx_admit_card_exam", columnList = "exam_id"),
        @Index(name = "idx_admit_card_student", columnList = "student_id"),
        @Index(name = "idx_admit_card_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AttributeOverride(name = "id", column = @Column(name = "admit_card_id"))
public class AdmitCard extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private AdmitCardStatus status = AdmitCardStatus.DRAFT;

    @Column(name = "pdf_url", length = 1000)
    private String pdfUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    private User publishedBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @OneToMany(mappedBy = "admitCard", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AdmitCardEntry> entries = new ArrayList<>();
}

