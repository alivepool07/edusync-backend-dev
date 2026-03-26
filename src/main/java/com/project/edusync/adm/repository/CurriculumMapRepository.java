package com.project.edusync.adm.repository;

import com.project.edusync.adm.model.entity.CurriculumMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CurriculumMapRepository extends JpaRepository<CurriculumMap, Long> {

    interface CurriculumOverviewProjection {
        UUID getClassId();

        String getClassName();

        Long getTotalSubjects();

        Long getTotalPeriodsPerWeek();

        Long getScheduledPeriods();
    }

    @Query("""
            SELECT cm FROM CurriculumMap cm
            JOIN FETCH cm.subject sub
            WHERE cm.academicClass.uuid = :classId
              AND cm.isActive = true
            ORDER BY sub.name ASC
            """)
    List<CurriculumMap> findActiveByClassUuid(UUID classId);

    @Query("""
            SELECT cm FROM CurriculumMap cm
            JOIN FETCH cm.subject sub
            JOIN FETCH cm.academicClass ac
            WHERE cm.uuid = :curriculumMapId
              AND cm.isActive = true
            """)
    Optional<CurriculumMap> findActiveByUuid(UUID curriculumMapId);

    @Query("""
            SELECT CASE WHEN COUNT(cm) > 0 THEN true ELSE false END
            FROM CurriculumMap cm
            WHERE cm.academicClass.uuid = :classId
              AND cm.subject.uuid = :subjectId
              AND cm.isActive = true
            """)
    boolean existsActiveByClassAndSubject(UUID classId, UUID subjectId);

    @Query("""
            SELECT ac.uuid as classId,
                   ac.name as className,
                   COUNT(cm.id) as totalSubjects,
                   COALESCE(SUM(cm.periodsPerWeek), 0) as totalPeriodsPerWeek,
                   COALESCE(COUNT(s.id), 0) as scheduledPeriods
            FROM CurriculumMap cm
            JOIN cm.academicClass ac
            LEFT JOIN Schedule s
                   ON s.isActive = true
                  AND s.section.academicClass = ac
                  AND s.subject = cm.subject
            WHERE cm.isActive = true
            GROUP BY ac.uuid, ac.name
            ORDER BY ac.name ASC
            """)
    List<CurriculumOverviewProjection> getCurriculumOverview();
}

