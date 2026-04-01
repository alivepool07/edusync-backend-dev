package com.project.edusync.superadmin.audit.service;

import com.project.edusync.superadmin.audit.model.entity.AuditLog;
import com.project.edusync.superadmin.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditAsyncWriter {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Transactional
    public void write(AuditLog logEntry) {
        auditLogRepository.save(logEntry);
    }
}

