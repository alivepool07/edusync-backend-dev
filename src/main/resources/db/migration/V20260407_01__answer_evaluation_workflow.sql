-- Answer sheet evaluation workflow tables.
CREATE TABLE IF NOT EXISTS evaluation_assignments (
    assignment_id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    schedule_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ASSIGNED',
    assigned_at TIMESTAMP NOT NULL,
    due_date DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uq_evaluation_assignments_schedule_teacher UNIQUE (schedule_id, teacher_id),
    CONSTRAINT fk_evaluation_assignments_schedule FOREIGN KEY (schedule_id) REFERENCES exam_schedule(id),
    CONSTRAINT fk_evaluation_assignments_teacher FOREIGN KEY (teacher_id) REFERENCES staff(id)
);

CREATE INDEX IF NOT EXISTS idx_evaluation_assignments_schedule ON evaluation_assignments(schedule_id);
CREATE INDEX IF NOT EXISTS idx_evaluation_assignments_teacher ON evaluation_assignments(teacher_id);

CREATE TABLE IF NOT EXISTS answer_sheets (
    answer_sheet_id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    student_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    uploaded_by_teacher_id BIGINT NOT NULL,
    file_url VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UPLOADED',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uq_answer_sheets_student_schedule UNIQUE (student_id, schedule_id),
    CONSTRAINT fk_answer_sheets_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_answer_sheets_schedule FOREIGN KEY (schedule_id) REFERENCES exam_schedule(id),
    CONSTRAINT fk_answer_sheets_uploaded_by_teacher FOREIGN KEY (uploaded_by_teacher_id) REFERENCES staff(id)
);

CREATE INDEX IF NOT EXISTS idx_answer_sheets_schedule ON answer_sheets(schedule_id);
CREATE INDEX IF NOT EXISTS idx_answer_sheets_status ON answer_sheets(status);

CREATE TABLE IF NOT EXISTS evaluation_results (
    evaluation_result_id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    answer_sheet_id BIGINT NOT NULL UNIQUE,
    total_marks NUMERIC(7,2) NOT NULL DEFAULT 0,
    status VARCHAR(10) NOT NULL DEFAULT 'DRAFT',
    evaluated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_evaluation_results_answer_sheet FOREIGN KEY (answer_sheet_id) REFERENCES answer_sheets(answer_sheet_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_evaluation_results_status ON evaluation_results(status);

CREATE TABLE IF NOT EXISTS question_marks (
    question_mark_id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    evaluation_result_id BIGINT NOT NULL,
    section_name VARCHAR(100) NOT NULL,
    question_number INTEGER NOT NULL,
    marks_obtained NUMERIC(7,2) NOT NULL,
    max_marks NUMERIC(7,2) NOT NULL,
    annotation_type VARCHAR(10) NOT NULL DEFAULT 'NONE',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uq_question_marks_result_section_question UNIQUE (evaluation_result_id, section_name, question_number),
    CONSTRAINT fk_question_marks_result FOREIGN KEY (evaluation_result_id) REFERENCES evaluation_results(evaluation_result_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_question_marks_result ON question_marks(evaluation_result_id);

CREATE TABLE IF NOT EXISTS answer_sheet_annotations (
    annotation_id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    answer_sheet_id BIGINT NOT NULL,
    page_number INTEGER NOT NULL,
    x DOUBLE PRECISION NOT NULL,
    y DOUBLE PRECISION NOT NULL,
    type VARCHAR(10) NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_answer_sheet_annotations_answer_sheet FOREIGN KEY (answer_sheet_id) REFERENCES answer_sheets(answer_sheet_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_answer_sheet_annotations_sheet_page ON answer_sheet_annotations(answer_sheet_id, page_number);

