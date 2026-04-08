package com.project.edusync.em.model.repository;

import com.project.edusync.em.model.entity.ExamTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExamTemplateRepository extends JpaRepository<ExamTemplate, Long> {

    Optional<ExamTemplate> findByUuid(UUID uuid);

    boolean existsByUuid(UUID uuid);

    @Query("""
            SELECT DISTINCT t FROM ExamTemplate t
            LEFT JOIN FETCH t.sections s
            ORDER BY t.createdAt DESC
            """)
    List<ExamTemplate> findAllWithSections();

    @Query("""
            SELECT t FROM ExamTemplate t
            LEFT JOIN FETCH t.sections
            WHERE t.uuid = :uuid
            """)
    Optional<ExamTemplate> findByUuidWithSections(@Param("uuid") UUID uuid);
}

