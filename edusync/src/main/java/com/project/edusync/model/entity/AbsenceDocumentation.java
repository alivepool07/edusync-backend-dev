package com.project.edusync.model.entity;

import com.project.edusync.ams.model.enums.ApprovalStatus; // Import NEW ENUM
import com.project.edusync.common.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "absence_documentation")
@Getter
@Setter
@NoArgsConstructor
public class AbsenceDocumentation{

    // id (as documentation_id), uuid, and audit fields are inherited.

    /**
     * Shared Primary Key and Foreign Key to StudentDailyAttendance.
     */

    @Id
    @Column(name = "daily_attendance_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "daily_attendance_id")
    private StudentDailyAttendance dailyAttendance;

    /** LOGICAL FK to uis.User.id **/
    @Column(name = "submitted_by_user_id", nullable = false)
    private Long submittedByUserId;

    /** LOGICAL FK to uis.Staff.id **/
    @Column(name = "approved_by_staff_id")
    private Long approvedByStaffId;

    @Column(name = "reason_text", columnDefinition = "TEXT", nullable = false)
    private String reasonText;

    @Column(name = "documentation_url", length = 255)
    private String documentationUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 10, nullable = false)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING; // Use NEW ENUM

    @Column(name = "reviewer_notes", columnDefinition = "TEXT")
    private String reviewerNotes;
}