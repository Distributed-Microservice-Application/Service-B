CREATE TABLE IF NOT EXISTS outbox (
    id BIGINT PRIMARY KEY,
    sum INT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_outbox_sum ON outbox (sum);

-- Insert the summation record with ID=1 if it doesn't exist
INSERT INTO outbox (id, sum) VALUES (1, 0) ON CONFLICT (id) DO NOTHING;
