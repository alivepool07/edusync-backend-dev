package com.project.edusync.model.entity;

import com.project.edusync.ams.model.enums.AttendanceSource; // Import NEW ENUM
import com.project.edusync.common.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "staff_daily_attendance",
        uniqueConstraints = @UniqueConstraint(columnNames = {"staff_id", "attendance_date"}))
@Getter
@Setter
@NoArgsConstructor
public class StaffDailyAttendance extends AuditableEntity {

    // id (as staff_attendance_id), uuid, and audit fields are inherited.

    /** LOGICAL FK to uis.Staff.id **/
    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_id", nullable = false)
    private AttendanceType attendanceType;

    @Column(name = "time_in")
    private LocalTime timeIn;

    @Column(name = "time_out")
    private LocalTime timeOut;

    @Column(name = "total_hours")
    private Double totalHours; // Use Double for DECIMAL(4, 2) mapping

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private AttendanceSource source = AttendanceSource.MANUAL; // Use NEW ENUM

    @Column(columnDefinition = "TEXT")
    private String notes;
}