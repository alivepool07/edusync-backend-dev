package com.project.edusync.adm.model.entity;

import com.project.edusync.common.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a class or grade (e.g., "Grade 9", "Grade 10").
 * Renamed to "AcademicClass" to avoid conflict with the Java "class" keyword.
 *
 * This entity extends AuditableEntity to gain ID (Long), UUID,
 * and audit timestamp fields.
 *
 * Relationships will be joined using the inherited 'id' (Long) primary key.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"sections", "curriculumMaps"}) // Exclude lazy relationships
@ToString(callSuper = true, exclude = {"sections", "curriculumMaps"}) // Exclude lazy relationships
@Entity
@Table(name = "classes") // Maps this entity to the "classes" table
public class AcademicClass extends AuditableEntity {

    // The @Id (Long id) and 'uuid' are inherited from AuditableEntity.

    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // --- Relationships ---

    /**
     * All sections that belong to this class (e.g., "Section A", "Section B").
     */
    @OneToMany(mappedBy = "academicClass", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Section> sections = new HashSet<>();

    @OneToMany(mappedBy = "academicClass", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<CurriculumMap> curriculumMaps = new HashSet<>();

}
