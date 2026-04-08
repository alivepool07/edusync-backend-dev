package com.project.edusync.em.model.repository;

import com.project.edusync.em.model.entity.AnswerSheetAnnotation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerSheetAnnotationRepository extends JpaRepository<AnswerSheetAnnotation, Long> {
    List<AnswerSheetAnnotation> findByAnswerSheetIdOrderByPageNumberAscIdAsc(Long answerSheetId);
}

