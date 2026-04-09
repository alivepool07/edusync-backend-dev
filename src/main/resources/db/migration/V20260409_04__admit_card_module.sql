CREATE TABLE IF NOT EXISTS admit_cards (
    admit_card_id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    student_id BIGINT NOT NULL,
    exam_id BIGINT NOT NULL,
    generated_at TIMESTAMP NOT NULL,
    status VARCHAR(15) NOT NULL DEFAULT 'DRAFT',
    pdf_url VARCHAR(1000),
    published_by BIGINT,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_admit_card_exam_student UNIQUE (exam_id, student_id),
    CONSTRAINT fk_admit_cards_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_admit_cards_exam FOREIGN KEY (exam_id) REFERENCES exams(exam_id),
    CONSTRAINT fk_admit_cards_published_by FOREIGN KEY (published_by) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_admit_card_exam ON admit_cards(exam_id);
CREATE INDEX IF NOT EXISTS idx_admit_card_student ON admit_cards(student_id);
CREATE INDEX IF NOT EXISTS idx_admit_card_status ON admit_cards(status);

CREATE TABLE IF NOT EXISTS admit_card_entries (
    id BIGSERIAL PRIMARY KEY,
    admit_card_id BIGINT NOT NULL,
    exam_schedule_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    subject_name VARCHAR(150) NOT NULL,
    exam_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room_id BIGINT NOT NULL,
    room_name VARCHAR(150),
    seat_id BIGINT NOT NULL,
    seat_label VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_admit_card_entries_card FOREIGN KEY (admit_card_id) REFERENCES admit_cards(admit_card_id) ON DELETE CASCADE,
    CONSTRAINT fk_admit_card_entries_schedule FOREIGN KEY (exam_schedule_id) REFERENCES exam_schedule(id)
);

CREATE INDEX IF NOT EXISTS idx_admit_card_entry_card ON admit_card_entries(admit_card_id);
CREATE INDEX IF NOT EXISTS idx_admit_card_entry_schedule ON admit_card_entries(exam_schedule_id);

