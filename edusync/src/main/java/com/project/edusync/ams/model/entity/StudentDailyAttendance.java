package com.project.edusync.ams.model.entity;

import com.project.edusync.common.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "student_daily_attendance",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "attendance_date"}))
@Getter
@Setter
@NoArgsConstructor
public class StudentDailyAttendance extends AuditableEntity {

    // id (as daily_attendance_id), uuid, and audit fields are inherited.

    /** LOGICAL FOREIGN KEY to uis.Student.id **/
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    /** LOGICAL FOREIGN KEY to uis.Staff.id **/
    @Column(name = "taken_by_staff_id", nullable = false)
    private Long takenByStaffId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_id", nullable = false)
    private AttendanceType attendanceType;

    @Column(columnDefinition = "TEXT")
    private String notes;
}