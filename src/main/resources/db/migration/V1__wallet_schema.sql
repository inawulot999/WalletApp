CREATE TABLE wallet_users (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    full_name VARCHAR(160) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(40) NOT NULL,
    country VARCHAR(2) NOT NULL,
    kyc_status VARCHAR(32) NOT NULL,
    wallet_status VARCHAR(32) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    transaction_pin_hash VARCHAR(255) NOT NULL,
    profile_image_url VARCHAR(512),
    two_factor_authenticator_enabled BOOLEAN NOT NULL,
    fingerprint_enabled BOOLEAN NOT NULL,
    pin_lock_enabled BOOLEAN NOT NULL,
    immediate_lock_enabled BOOLEAN NOT NULL,
    two_factor_secret VARCHAR(255),
    bvn VARCHAR(20),
    nin VARCHAR(20),
    residential_address VARCHAR(512)
);

CREATE TABLE money_accounts (
    reference VARCHAR(255) PRIMARY KEY,
    owner_reference VARCHAR(255) NOT NULL,
    currency VARCHAR(12) NOT NULL,
    balance NUMERIC(28, 8) NOT NULL
);
CREATE INDEX ix_money_accounts_owner_reference ON money_accounts(owner_reference);

CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    account_reference VARCHAR(255) NOT NULL,
    currency VARCHAR(12) NOT NULL,
    side VARCHAR(16) NOT NULL,
    amount NUMERIC(28, 8) NOT NULL,
    balance_after NUMERIC(28, 8) NOT NULL,
    memo VARCHAR(512)
);
CREATE INDEX ix_ledger_entries_account_created ON ledger_entries(account_reference, created_at DESC);

CREATE TABLE crypto_wallet_addresses (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    network VARCHAR(32) NOT NULL,
    address VARCHAR(255) NOT NULL UNIQUE,
    encrypted_private_key_reference VARCHAR(255) NOT NULL,
    seed_derivation_path VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_wallet_address_user_network UNIQUE(user_id, network)
);

CREATE TABLE transfer_records (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    source_user_id UUID NOT NULL,
    transfer_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    source_currency VARCHAR(12) NOT NULL,
    target_currency VARCHAR(12) NOT NULL,
    source_amount NUMERIC(28, 8) NOT NULL,
    fee_amount NUMERIC(28, 8) NOT NULL,
    exchange_rate NUMERIC(28, 8) NOT NULL,
    estimated_target_amount NUMERIC(28, 8) NOT NULL,
    recipient_name VARCHAR(255) NOT NULL,
    destination_country VARCHAR(64) NOT NULL,
    destination_reference VARCHAR(255) NOT NULL,
    compliance_note VARCHAR(512),
    asset_type VARCHAR(12),
    network VARCHAR(32),
    sender_address VARCHAR(255),
    recipient_address VARCHAR(255),
    total_deduction NUMERIC(28, 8),
    tx_hash VARCHAR(255),
    status_message VARCHAR(512)
);
CREATE INDEX ix_transfer_records_user_created ON transfer_records(source_user_id, created_at DESC);
