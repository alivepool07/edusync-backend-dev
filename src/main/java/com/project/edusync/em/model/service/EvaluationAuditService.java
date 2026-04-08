package com.project.edusync.em.model.service;

import com.project.edusync.em.model.entity.AnswerSheet;
import com.project.edusync.em.model.entity.EvaluationAssignment;
import com.project.edusync.em.model.entity.EvaluationResult;
import com.project.edusync.em.model.enums.EvaluationAuditEventType;
import com.project.edusync.uis.model.entity.Staff;

import java.util.Map;

public interface EvaluationAuditService {

    void record(EvaluationAuditEventType eventType,
                Staff actorTeacher,
                EvaluationAssignment assignment,
                AnswerSheet answerSheet,
                EvaluationResult evaluationResult,
                Map<String, Object> metadata);
}

