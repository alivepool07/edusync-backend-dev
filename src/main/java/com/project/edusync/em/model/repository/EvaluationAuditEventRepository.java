package com.project.edusync.em.model.repository;

import com.project.edusync.em.model.entity.EvaluationAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationAuditEventRepository extends JpaRepository<EvaluationAuditEvent, Long> {
}

