package com.project.edusync.em.model.repository;

import com.project.edusync.em.model.entity.AnswerSheetImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerSheetImageRepository extends JpaRepository<AnswerSheetImage, Long> {
    List<AnswerSheetImage> findByAnswerSheetIdOrderByPageNumberAsc(Long answerSheetId);
}

