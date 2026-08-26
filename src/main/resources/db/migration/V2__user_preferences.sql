ALTER TABLE wallet_users ADD COLUMN preferred_currency VARCHAR(3) NOT NULL DEFAULT 'USD';
ALTER TABLE wallet_users ADD COLUMN notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE;
