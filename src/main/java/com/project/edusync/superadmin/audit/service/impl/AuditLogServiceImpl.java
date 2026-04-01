package com.project.edusync.superadmin.audit.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.edusync.common.security.AuthUtil;
import com.project.edusync.iam.model.entity.User;
import com.project.edusync.superadmin.audit.model.dto.AuditLogResponseDto;
import com.project.edusync.superadmin.audit.model.entity.AuditLog;
import com.project.edusync.superadmin.audit.repository.AuditLogRepository;
import com.project.edusync.superadmin.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuthUtil authUtil;
    private final ObjectMapper objectMapper;

    @Override
    @Async
    @Transactional
    public void logAsync(String action,
                         String entityType,
                         String entityId,
                         String entityDisplayName,
                         Map<String, Object> changePayload,
                         String ipAddress,
                         String userAgent) {
        try {
            String actorUsername = "system";
            String actorRole = "SYSTEM";
            try {
                User actor = authUtil.getCurrentUser();
                actorUsername = actor.getUsername();
                actorRole = actor.getRoles().stream()
                        .map(role -> role.getName() == null ? "" : role.getName())
                        .findFirst()
                        .orElse("SYSTEM");
                if (actorRole.startsWith("ROLE_")) {
                    actorRole = actorRole.substring(5);
                }
            } catch (Exception ignored) {
                // background/system calls may not have auth context
            }

            AuditLog logEntry = new AuditLog();
            logEntry.setActorUsername(actorUsername);
            logEntry.setActorRole(actorRole);
            logEntry.setAction(action);
            logEntry.setEntityType(entityType);
            logEntry.setEntityId(entityId);
            logEntry.setEntityDisplayName(entityDisplayName);
            logEntry.setChangePayload(serializePayload(changePayload));
            logEntry.setIpAddress(ipAddress);
            logEntry.setUserAgent(userAgent);
            logEntry.setTimestamp(Instant.now());
            auditLogRepository.save(logEntry);
        } catch (Exception ex) {
            log.error("Failed to write audit log action={} entityType={} entityId={}", action, entityType, entityId, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponseDto> search(String actor,
                                            String action,
                                            String entityType,
                                            Instant from,
                                            Instant to,
                                            Pageable pageable) {

        Specification<AuditLog> spec = Specification.where(null);

        if (StringUtils.hasText(actor)) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("actorUsername")), "%" + actor.trim().toLowerCase() + "%"));
        }
        if (StringUtils.hasText(action)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("action"), action.trim()));
        }
        if (StringUtils.hasText(entityType)) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("entityType"), entityType.trim()));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("timestamp"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("timestamp"), to));
        }

        return auditLogRepository.findAll(spec, pageable).map(this::toResponse);
    }

    private String serializePayload(Map<String, Object> payload) {
        Map<String, Object> source = payload == null ? Map.of("before", Map.of(), "after", Map.of()) : sanitize(payload);
        try {
            return objectMapper.writeValueAsString(source);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private JsonNode parsePayload(String raw) {
        try {
            return objectMapper.readTree(raw == null ? "{}" : raw);
        } catch (JsonProcessingException ex) {
            return objectMapper.createObjectNode();
        }
    }

    private AuditLogResponseDto toResponse(AuditLog logEntry) {
        return new AuditLogResponseDto(
                logEntry.getId(),
                logEntry.getActorUsername(),
                logEntry.getActorRole(),
                logEntry.getAction(),
                logEntry.getEntityType(),
                logEntry.getEntityId(),
                logEntry.getEntityDisplayName(),
                parsePayload(logEntry.getChangePayload()),
                logEntry.getIpAddress(),
                logEntry.getTimestamp()
        );
    }

    private Map<String, Object> sanitize(Map<String, Object> payload) {
        Map<String, Object> safe = new HashMap<>(payload);
        maskSensitive(safe, "password");
        maskSensitive(safe, "token");
        maskSensitive(safe, "secret");
        return safe;
    }

    private void maskSensitive(Map<String, Object> map, String token) {
        map.replaceAll((key, value) -> {
            if (key != null && key.toLowerCase().contains(token)) {
                return "***";
            }
            return value;
        });
    }
}

