package com.project.edusync.adm.repository;

import com.project.edusync.adm.model.entity.AcademicClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface AcademicClassRepository extends JpaRepository<AcademicClass, Long> {

    @Query("SELECT c from AcademicClass c WHERE c.uuid = :classId")
    Optional<AcademicClass> findById(UUID classId);
}
