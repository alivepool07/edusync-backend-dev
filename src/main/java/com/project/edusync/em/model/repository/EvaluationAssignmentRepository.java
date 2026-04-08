package com.project.edusync.em.model.repository;

import com.project.edusync.em.model.entity.EvaluationAssignment;
import com.project.edusync.em.model.enums.EvaluationAssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EvaluationAssignmentRepository extends JpaRepository<EvaluationAssignment, Long> {

    @Query("""
            SELECT ea FROM EvaluationAssignment ea
            JOIN FETCH ea.examSchedule es
            JOIN FETCH es.exam ex
            JOIN FETCH es.subject sub
            JOIN FETCH ea.teacher t
            WHERE t.id = :teacherId
            ORDER BY ea.assignedAt DESC
            """)
    List<EvaluationAssignment> findAllByTeacherIdWithSchedule(@Param("teacherId") Long teacherId);

    @Query("""
            SELECT ea FROM EvaluationAssignment ea
            JOIN FETCH ea.examSchedule es
            JOIN FETCH es.exam ex
            JOIN FETCH es.subject sub
            JOIN FETCH ea.teacher t
            WHERE (:teacherId IS NULL OR t.id = :teacherId)
            ORDER BY ea.assignedAt DESC
            """)
    List<EvaluationAssignment> findAllWithSchedule(@Param("teacherId") Long teacherId);

    Optional<EvaluationAssignment> findByExamScheduleIdAndTeacherId(Long scheduleId, Long teacherId);

    boolean existsByExamScheduleIdAndTeacherId(Long scheduleId, Long teacherId);

    long countByExamScheduleIdAndTeacherIdAndStatusNot(Long scheduleId, Long teacherId, EvaluationAssignmentStatus status);
}

