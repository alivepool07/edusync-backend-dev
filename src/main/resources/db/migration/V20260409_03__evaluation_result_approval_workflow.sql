-- Expand evaluation_results lifecycle from DRAFT/FINAL to approval workflow states.
ALTER TABLE evaluation_results
    ALTER COLUMN status TYPE VARCHAR(20);

ALTER TABLE evaluation_results
    ADD COLUMN IF NOT EXISTS submitted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS published_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS approved_by BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_evaluation_results_approved_by'
          AND table_name = 'evaluation_results'
    ) THEN
        ALTER TABLE evaluation_results
            ADD CONSTRAINT fk_evaluation_results_approved_by
            FOREIGN KEY (approved_by) REFERENCES users(id);
    END IF;
END
$$;

-- Preserve historical FINAL visibility by mapping FINAL -> PUBLISHED.
UPDATE evaluation_results
SET status = 'PUBLISHED',
    published_at = COALESCE(published_at, evaluated_at, updated_at, created_at)
WHERE status = 'FINAL';

CREATE INDEX IF NOT EXISTS idx_evaluation_results_approved_by ON evaluation_results(approved_by);
CREATE INDEX IF NOT EXISTS idx_evaluation_results_status_published_at ON evaluation_results(status, published_at DESC);

