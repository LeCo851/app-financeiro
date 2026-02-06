ALTER TABLE transactions
    ADD COLUMN is_fixed_expense BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_transactions_fixed_pattern
    ON transactions (user_id, is_fixed_expense, date);