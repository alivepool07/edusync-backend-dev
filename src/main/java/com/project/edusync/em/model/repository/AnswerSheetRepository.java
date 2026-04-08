package com.project.edusync.em.model.repository;

import com.project.edusync.em.model.entity.AnswerSheet;
import com.project.edusync.em.model.enums.AnswerSheetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnswerSheetRepository extends JpaRepository<AnswerSheet, Long> {

    @Query("""
            SELECT a FROM AnswerSheet a
            JOIN FETCH a.examSchedule es
            JOIN FETCH es.exam ex
            JOIN FETCH es.subject sub
            JOIN FETCH a.student st
            JOIN FETCH st.userProfile up
            WHERE a.id = :answerSheetId
            """)
    Optional<AnswerSheet> findByIdWithScheduleAndStudent(@Param("answerSheetId") Long answerSheetId);

    @Query(value = """
            SELECT a FROM AnswerSheet a
            JOIN a.student st
            JOIN st.userProfile up
            WHERE a.examSchedule.id = :scheduleId
            ORDER BY a.createdAt DESC
            """,
            countQuery = """
            SELECT COUNT(a) FROM AnswerSheet a
            WHERE a.examSchedule.id = :scheduleId
            """)
    Page<AnswerSheet> findByScheduleId(@Param("scheduleId") Long scheduleId, Pageable pageable);

    List<AnswerSheet> findByExamScheduleId(Long scheduleId);

    Optional<AnswerSheet> findByExamScheduleIdAndStudentId(Long scheduleId, Long studentId);

    boolean existsByExamScheduleIdAndStudentId(Long scheduleId, Long studentId);

    long countByExamScheduleIdAndUploadedByTeacherIdAndStatus(Long scheduleId, Long teacherId, AnswerSheetStatus status);
}

