package com.project.edusync.model.entity;

import com.project.edusync.common.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "attendance_audit")
@Getter
@Setter
@NoArgsConstructor
public class AttendanceAudit extends AuditableEntity {

    // id (as audit_log_id), uuid, and audit fields are inherited.

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "daily_attendance_id", nullable = false)
    private StudentDailyAttendance dailyAttendance;

    @Column(name = "audited_table", length = 50, nullable = false)
    private String auditedTable = "student_daily_attendance";

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 10, nullable = false)
    private ActionType actionType;

    @Column(name = "column_name", length = 100)
    private String columnName;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /** LOGICAL FOREIGN KEY to uis.User.id **/
    @Column(name = "changed_by_user_id", nullable = false)
    private Long changedByUserId;

    public enum ActionType {
        INSERT,
        UPDATE,
        DELETE
    }
}