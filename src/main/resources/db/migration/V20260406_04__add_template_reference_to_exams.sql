-- Add template reference on exams so each exam is tied to a template.
ALTER TABLE exams
    ADD COLUMN IF NOT EXISTS template_id BIGINT;

ALTER TABLE exams
    DROP CONSTRAINT IF EXISTS fk_exams_template;

ALTER TABLE exams
    ADD CONSTRAINT fk_exams_template
    FOREIGN KEY (template_id) REFERENCES exam_template(template_id);

CREATE INDEX IF NOT EXISTS idx_exams_template_id ON exams(template_id);

