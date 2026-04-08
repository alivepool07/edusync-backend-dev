-- Exam template master with immutable schedule snapshot support.
CREATE TABLE IF NOT EXISTS exam_template (
    template_id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    total_marks INTEGER NOT NULL,
    total_questions INTEGER NOT NULL,
    in_use BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS template_section (
    section_id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    template_id BIGINT NOT NULL,
    section_name VARCHAR(80) NOT NULL,
    section_order INTEGER NOT NULL,
    question_count INTEGER NOT NULL,
    marks_per_question INTEGER NOT NULL,
    is_objective BOOLEAN,
    is_subjective BOOLEAN,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_template_section_template FOREIGN KEY (template_id)
        REFERENCES exam_template(template_id) ON DELETE CASCADE,
    CONSTRAINT uq_template_section_order UNIQUE (template_id, section_order)
);

ALTER TABLE exam_schedule
    ADD COLUMN IF NOT EXISTS template_id BIGINT,
    ADD COLUMN IF NOT EXISTS template_snapshot JSONB;

ALTER TABLE exam_schedule
    DROP CONSTRAINT IF EXISTS fk_exam_schedule_template;

ALTER TABLE exam_schedule
    ADD CONSTRAINT fk_exam_schedule_template
    FOREIGN KEY (template_id) REFERENCES exam_template(template_id);

CREATE INDEX IF NOT EXISTS idx_exam_schedule_template_id ON exam_schedule(template_id);

