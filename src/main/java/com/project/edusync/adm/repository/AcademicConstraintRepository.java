package com.project.edusync.adm.repository;

import com.project.edusync.adm.model.entity.AcademicConstraint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademicConstraintRepository extends JpaRepository<AcademicConstraint, Long> {

    Optional<AcademicConstraint> findTopByConstraintTypeIgnoreCaseAndIsActiveTrue(String constraintType);
}
