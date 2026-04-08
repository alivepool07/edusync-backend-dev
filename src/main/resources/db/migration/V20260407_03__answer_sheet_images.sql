-- Supports multi-page image answer sheets and explicit upload completion state.
ALTER TABLE answer_sheets
    ALTER COLUMN file_url DROP NOT NULL;

ALTER TABLE answer_sheets
    ALTER COLUMN file_url TYPE VARCHAR(1000);

CREATE TABLE IF NOT EXISTS answer_sheet_images (
    answer_sheet_image_id BIGSERIAL PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    answer_sheet_id BIGINT NOT NULL,
    page_number INTEGER NOT NULL,
    image_url VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uq_answer_sheet_images_sheet_page UNIQUE (answer_sheet_id, page_number),
    CONSTRAINT fk_answer_sheet_images_answer_sheet FOREIGN KEY (answer_sheet_id)
        REFERENCES answer_sheets(answer_sheet_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_answer_sheet_images_sheet_page
    ON answer_sheet_images(answer_sheet_id, page_number);

