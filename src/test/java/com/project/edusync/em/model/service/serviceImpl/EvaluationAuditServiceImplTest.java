package com.project.edusync.em.model.service.serviceImpl;

import com.project.edusync.em.model.entity.EvaluationAuditEvent;
import com.project.edusync.em.model.enums.EvaluationAuditEventType;
import com.project.edusync.em.model.repository.EvaluationAuditEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EvaluationAuditServiceImplTest {

    @Mock
    private EvaluationAuditEventRepository auditEventRepository;

    @InjectMocks
    private EvaluationAuditServiceImpl evaluationAuditService;

    @Test
    void recordSavesAuditEventWithSanitizedMetadata() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("status", "DRAFT");
        metadata.put("nullValue", null);
        metadata.put(null, "value");

        evaluationAuditService.record(
                EvaluationAuditEventType.DRAFT_MARKS_SAVED,
                null,
                null,
                null,
                null,
                metadata
        );

        ArgumentCaptor<EvaluationAuditEvent> captor = ArgumentCaptor.forClass(EvaluationAuditEvent.class);
        verify(auditEventRepository).save(captor.capture());

        EvaluationAuditEvent savedEvent = captor.getValue();
        assertEquals(EvaluationAuditEventType.DRAFT_MARKS_SAVED, savedEvent.getEventType());
        assertNotNull(savedEvent.getEventMetadata());
        assertEquals("DRAFT", savedEvent.getEventMetadata().get("status"));
        assertFalse(savedEvent.getEventMetadata().containsKey("nullValue"));
        assertFalse(savedEvent.getEventMetadata().containsKey(null));
    }

    @Test
    void recordTruncatesOverlyLongStringMetadataValues() {
        String longText = "x".repeat(700);
        Map<String, Object> metadata = Map.of("note", longText);

        evaluationAuditService.record(
                EvaluationAuditEventType.ANNOTATION_CREATED,
                null,
                null,
                null,
                null,
                metadata
        );

        ArgumentCaptor<EvaluationAuditEvent> captor = ArgumentCaptor.forClass(EvaluationAuditEvent.class);
        verify(auditEventRepository).save(captor.capture());

        EvaluationAuditEvent savedEvent = captor.getValue();
        String note = (String) savedEvent.getEventMetadata().get("note");
        assertEquals(500, note.length());
    }
}


