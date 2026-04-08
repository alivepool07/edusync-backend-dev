package com.project.edusync.em.model.repository;

import com.project.edusync.em.model.entity.EvaluationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EvaluationResultRepository extends JpaRepository<EvaluationResult, Long> {

    Optional<EvaluationResult> findByAnswerSheetId(Long answerSheetId);

    @Query("""
            SELECT er FROM EvaluationResult er
            JOIN FETCH er.answerSheet a
            WHERE a.id = :answerSheetId
            """)
    Optional<EvaluationResult> findByAnswerSheetIdWithAnswerSheet(@Param("answerSheetId") Long answerSheetId);
}

