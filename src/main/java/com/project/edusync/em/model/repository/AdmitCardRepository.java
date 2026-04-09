package com.project.edusync.em.model.repository;

import com.project.edusync.em.model.entity.AdmitCard;
import com.project.edusync.em.model.enums.AdmitCardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AdmitCardRepository extends JpaRepository<AdmitCard, Long> {

    @Query("""
            SELECT ac FROM AdmitCard ac
            JOIN FETCH ac.student st
            JOIN FETCH st.userProfile up
            JOIN FETCH ac.exam ex
            WHERE ex.id = :examId
            ORDER BY up.firstName ASC, up.lastName ASC
            """)
    List<AdmitCard> findByExamIdWithStudent(@Param("examId") Long examId);

    @Query("""
            SELECT ac FROM AdmitCard ac
            JOIN FETCH ac.student st
            JOIN FETCH st.userProfile up
            JOIN FETCH ac.exam ex
            WHERE ex.id = :examId
              AND st.id = :studentId
            """)
    Optional<AdmitCard> findByExamIdAndStudentIdWithContext(@Param("examId") Long examId,
                                                            @Param("studentId") Long studentId);

    @Query("""
            SELECT ac FROM AdmitCard ac
            JOIN FETCH ac.student st
            JOIN FETCH st.userProfile up
            JOIN FETCH ac.exam ex
            WHERE ex.id = :examId
              AND ac.status = :status
            ORDER BY up.firstName ASC, up.lastName ASC
            """)
    List<AdmitCard> findByExamIdAndStatusWithStudent(@Param("examId") Long examId,
                                                     @Param("status") AdmitCardStatus status);

    @Query("""
            SELECT ac FROM AdmitCard ac
            JOIN FETCH ac.student st
            JOIN FETCH st.userProfile up
            JOIN FETCH ac.exam ex
            WHERE ex.id = :examId
              AND st.id IN :studentIds
            """)
    List<AdmitCard> findByExamIdAndStudentIds(@Param("examId") Long examId,
                                              @Param("studentIds") List<Long> studentIds);

    @Query("""
            SELECT e.examSchedule.id, COUNT(DISTINCT ac.student.id)
            FROM AdmitCard ac
            JOIN ac.entries e
            WHERE ac.exam.id = :examId
            GROUP BY e.examSchedule.id
            """)
    List<Object[]> countGeneratedPerSchedule(@Param("examId") Long examId);

    @Query("""
            SELECT MAX(ac.generatedAt)
            FROM AdmitCard ac
            JOIN ac.entries e
            WHERE ac.exam.id = :examId AND e.examSchedule.id = :scheduleId
            """)
    Optional<java.time.LocalDateTime> findLastGeneratedAtForSchedule(@Param("examId") Long examId,
                                                                      @Param("scheduleId") Long scheduleId);

    @Query("""
            SELECT e.examSchedule.id, COUNT(DISTINCT ac.student.id)
            FROM AdmitCard ac
            JOIN ac.entries e
            WHERE ac.exam.id = :examId AND ac.status = 'PUBLISHED'
            GROUP BY e.examSchedule.id
            """)
    List<Object[]> countPublishedPerSchedule(@Param("examId") Long examId);

    @Query("""
            SELECT DISTINCT ac FROM AdmitCard ac
            JOIN FETCH ac.student st
            JOIN FETCH st.userProfile up
            JOIN FETCH ac.exam ex
            JOIN ac.entries e
            WHERE ex.id = :examId
              AND ac.status = 'DRAFT'
              AND e.examSchedule.id IN :scheduleIds
            """)
    List<AdmitCard> findDraftCardsByExamAndScheduleIds(@Param("examId") Long examId,
                                                       @Param("scheduleIds") List<Long> scheduleIds);

    long deleteByExam_Id(Long examId);
}
