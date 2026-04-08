package com.project.edusync.em.model.service.serviceImpl;

import com.project.edusync.em.model.entity.AnswerSheet;
import com.project.edusync.em.model.entity.EvaluationAssignment;
import com.project.edusync.em.model.entity.EvaluationAuditEvent;
import com.project.edusync.em.model.entity.EvaluationResult;
import com.project.edusync.em.model.enums.EvaluationAuditEventType;
import com.project.edusync.em.model.repository.EvaluationAuditEventRepository;
import com.project.edusync.em.model.service.EvaluationAuditService;
import com.project.edusync.uis.model.entity.Staff;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EvaluationAuditServiceImpl implements EvaluationAuditService {

    private static final int MAX_VALUE_LENGTH = 500;

    private final EvaluationAuditEventRepository auditEventRepository;

    @Override
    public void record(EvaluationAuditEventType eventType,
                       Staff actorTeacher,
                       EvaluationAssignment assignment,
                       AnswerSheet answerSheet,
                       EvaluationResult evaluationResult,
                       Map<String, Object> metadata) {
        EvaluationAuditEvent event = EvaluationAuditEvent.builder()
                .eventType(eventType)
                .actorTeacher(actorTeacher)
                .assignment(assignment)
                .answerSheet(answerSheet)
                .evaluationResult(evaluationResult)
                .eventMetadata(sanitizeMetadata(metadata))
                .occurredAt(LocalDateTime.now())
                .build();
        auditEventRepository.save(event);
    }

    private Map<String, Object> sanitizeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        Map<String, Object> sanitized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            Object value = entry.getValue();
            if (value instanceof String stringValue && stringValue.length() > MAX_VALUE_LENGTH) {
                sanitized.put(entry.getKey(), stringValue.substring(0, MAX_VALUE_LENGTH));
                continue;
            }
            sanitized.put(entry.getKey(), value);
        }

        return sanitized.isEmpty() ? null : sanitized;
    }
}

