ALTER TABLE donation_history
    DROP COLUMN IF EXISTS update_time,
    DROP COLUMN IF EXISTS created_by,
    DROP COLUMN IF EXISTS modified_by;
