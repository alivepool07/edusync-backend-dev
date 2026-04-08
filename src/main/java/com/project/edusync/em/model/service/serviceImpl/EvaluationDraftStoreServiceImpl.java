package com.project.edusync.em.model.service.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.edusync.em.model.dto.RequestDTO.SaveQuestionMarkRequestDTO;
import com.project.edusync.em.model.service.EvaluationDraftStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationDraftStoreServiceImpl implements EvaluationDraftStoreService {

    private static final String DRAFT_KEY_PREFIX = "evaluation:draft:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.evaluation.draft.ttl-minutes:30}")
    private long draftTtlMinutes;

    @Override
    public void saveDraft(Long answerSheetId, List<SaveQuestionMarkRequestDTO> questionMarks) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("answerSheetId", answerSheetId);
        payload.put("updatedAt", LocalDateTime.now());
        payload.put("questionMarks", questionMarks);

        try {
            stringRedisTemplate.opsForValue().set(
                    buildKey(answerSheetId),
                    objectMapper.writeValueAsString(payload),
                    Duration.ofMinutes(Math.max(draftTtlMinutes, 1))
            );
        } catch (JsonProcessingException ex) {
            log.warn("Skipping evaluation draft cache write for answerSheetId={} due to serialization error: {}", answerSheetId, ex.getMessage());
        } catch (RuntimeException ex) {
            log.warn("Skipping evaluation draft cache write for answerSheetId={} due to Redis error: {}", answerSheetId, ex.getMessage());
        }
    }

    @Override
    public List<SaveQuestionMarkRequestDTO> getDraft(Long answerSheetId) {
        try {
            String raw = stringRedisTemplate.opsForValue().get(buildKey(answerSheetId));
            if (raw == null || raw.isBlank()) {
                return Collections.emptyList();
            }

            Map<String, Object> payload = objectMapper.readValue(raw, new TypeReference<>() {
            });
            Object marks = payload.get("questionMarks");
            if (marks == null) {
                return Collections.emptyList();
            }
            return objectMapper.convertValue(marks, new TypeReference<>() {
            });
        } catch (RuntimeException | JsonProcessingException ex) {
            log.warn("Skipping evaluation draft cache read for answerSheetId={} due to error: {}", answerSheetId, ex.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public void deleteDraft(Long answerSheetId) {
        try {
            stringRedisTemplate.delete(buildKey(answerSheetId));
        } catch (RuntimeException ex) {
            log.warn("Failed to delete evaluation draft cache for answerSheetId={}: {}", answerSheetId, ex.getMessage());
        }
    }

    private String buildKey(Long answerSheetId) {
        return DRAFT_KEY_PREFIX + answerSheetId;
    }
}

