package com.project.edusync.adm.model.entity;

import com.project.edusync.common.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"academicConstraints", "schedules", "curriculumMaps"}) // Use inherited fields, exclude relationships
@ToString(callSuper = true, exclude = {"academicConstraints", "schedules", "curriculumMaps"}) // Use inherited fields, exclude relationships
@NoArgsConstructor
public class Subject extends AuditableEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "subject_code", unique = true, length = 50)
    private String subjectCode;

    @Column(name = "requires_special_room_type", length = 100)
    private String requiresSpecialRoomType;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // --- Relationships ---

    /**
     * All constraints that are associated with this subject.
     * The 'Constraint' entity will map this relationship via its 'subject' field,
     * which will join on this entity's 'id' (Long) column.
     */
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<AcademicConstraint> academicConstraints = new HashSet<>();

    /**
     * All schedule entries where this subject is being taught.
     * The 'Schedule' entity will map this relationship via its 'subject' field,
     * which will join on this entity's 'id' (Long) column.
     */
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Schedule> schedules = new HashSet<>();

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<CurriculumMap> curriculumMaps = new HashSet<>();
}
