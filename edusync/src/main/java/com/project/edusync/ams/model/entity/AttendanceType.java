package com.project.edusync.ams.model.entity;

import com.project.edusync.common.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "attendance_types")
@Getter
@Setter
@NoArgsConstructor
public class AttendanceType extends AuditableEntity {

    // id (as type_id), uuid, and audit fields are inherited.

    @Column(name = "type_name", length = 50, nullable = false, unique = true)
    private String typeName; // e.g., "Present", "Unexcused Absence"

    @Column(name = "short_code", length = 10, nullable = false, unique = true)
    private String shortCode; // e.g., "P", "A", "UA", "E"

    @Column(name = "is_present_mark", nullable = false)
    private boolean isPresentMark = false;

    @Column(name = "is_absence_mark", nullable = false)
    private boolean isAbsenceMark = false;

    @Column(name = "is_late_mark", nullable = false)
    private boolean isLateMark = false;

    @Column(name = "color_code", length = 7)
    private String colorCode; // Hex color for UI display
}
