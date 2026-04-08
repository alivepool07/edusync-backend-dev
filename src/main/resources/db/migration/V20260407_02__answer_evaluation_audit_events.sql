-- Immutable audit trail for answer evaluation write actions.
CREATE TABLE IF NOT EXISTS evaluation_audit_events (
    audit_event_id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    event_type VARCHAR(50) NOT NULL,
    actor_teacher_id BIGINT,
    assignment_id BIGINT,
    answer_sheet_id BIGINT,
    evaluation_result_id BIGINT,
    event_metadata JSONB,
    occurred_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_evaluation_audit_actor_teacher FOREIGN KEY (actor_teacher_id) REFERENCES staff(id),
    CONSTRAINT fk_evaluation_audit_assignment FOREIGN KEY (assignment_id) REFERENCES evaluation_assignments(assignment_id) ON DELETE SET NULL,
    CONSTRAINT fk_evaluation_audit_answer_sheet FOREIGN KEY (answer_sheet_id) REFERENCES answer_sheets(answer_sheet_id) ON DELETE SET NULL,
    CONSTRAINT fk_evaluation_audit_result FOREIGN KEY (evaluation_result_id) REFERENCES evaluation_results(evaluation_result_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_evaluation_audit_event_type ON evaluation_audit_events(event_type);
CREATE INDEX IF NOT EXISTS idx_evaluation_audit_occurred_at ON evaluation_audit_events(occurred_at DESC);
CREATE INDEX IF NOT EXISTS idx_evaluation_audit_answer_sheet ON evaluation_audit_events(answer_sheet_id);
CREATE INDEX IF NOT EXISTS idx_evaluation_audit_assignment ON evaluation_audit_events(assignment_id);

