ALTER TABLE wallet_users ADD COLUMN password_reset_token_hash VARCHAR(255);
ALTER TABLE wallet_users ADD COLUMN password_reset_expires_at TIMESTAMPTZ;
