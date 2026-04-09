-- Hardening constraints for admit card tables without mutating prior migration checksum.

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_admit_cards_status'
    ) THEN
        ALTER TABLE admit_cards
            ADD CONSTRAINT ck_admit_cards_status
            CHECK (status IN ('DRAFT', 'PUBLISHED'));
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_admit_card_entries_time_window'
    ) THEN
        ALTER TABLE admit_card_entries
            ADD CONSTRAINT ck_admit_card_entries_time_window
            CHECK (start_time < end_time);
    END IF;
END
$$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_admit_card_entry_card_schedule
    ON admit_card_entries (admit_card_id, exam_schedule_id);

