-- Schema bootstrap for Telecom System (account recycling enabled)
DROP TABLE IF EXISTS login_info;
DROP TABLE IF EXISTS recycled_accounts;
DROP TABLE IF EXISTS admin_info;
DROP TABLE IF EXISTS user_info;
DROP TABLE IF EXISTS package_info;
DROP SEQUENCE IF EXISTS user_info_account_seq;

-- Sequence starts at 20001 to satisfy business requirement
CREATE SEQUENCE user_info_account_seq
    START 20001
    INCREMENT 1
    MINVALUE 20001;

-- Packages
CREATE TABLE package_info (
    id INT PRIMARY KEY,
    duration INTERVAL NOT NULL,
    cost DECIMAL(10,2) NOT NULL
);

-- Users (account assigned via sequence or recycle pool)
CREATE TABLE user_info (
    account INT PRIMARY KEY DEFAULT nextval('user_info_account_seq'),
    name VARCHAR(20) NOT NULL,
    password VARCHAR(60) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    balance DECIMAL(10,2) NOT NULL,
    package_id INT NOT NULL REFERENCES package_info(id),
    package_start_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Admins
CREATE TABLE admin_info (
    account INT PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    password VARCHAR(60) NOT NULL
);

-- Login sessions
CREATE TABLE login_info (
    account_id INT NOT NULL REFERENCES user_info(account),
    login_time TIMESTAMPTZ NOT NULL,
    logout_time TIMESTAMPTZ,
    PRIMARY KEY (account_id, login_time)
);

-- Pool of recycled accounts
CREATE TABLE recycled_accounts (
    account INT PRIMARY KEY,
    recycled_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Trigger: allocate account (reuse recycled first)
CREATE OR REPLACE FUNCTION allocate_user_account()
RETURNS TRIGGER AS $$
DECLARE
    reused_account INT;
BEGIN
    IF NEW.account IS NOT NULL THEN
        RETURN NEW;
    END IF;

    SELECT account INTO reused_account
    FROM recycled_accounts
    ORDER BY recycled_at
    LIMIT 1
    FOR UPDATE SKIP LOCKED;

    IF reused_account IS NOT NULL THEN
        DELETE FROM recycled_accounts WHERE account = reused_account;
        NEW.account := reused_account;
    ELSE
        NEW.account := nextval('user_info_account_seq');
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER assign_user_account_trg
    BEFORE INSERT ON user_info
    FOR EACH ROW
    EXECUTE FUNCTION allocate_user_account();

-- Trigger: recycle account on delete and clean login_info
CREATE OR REPLACE FUNCTION recycle_user_account()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO recycled_accounts(account, recycled_at)
    VALUES (OLD.account, CURRENT_TIMESTAMP)
    ON CONFLICT (account) DO UPDATE SET recycled_at = EXCLUDED.recycled_at;

    DELETE FROM login_info WHERE account_id = OLD.account;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER recycle_user_account_trg
    BEFORE DELETE ON user_info
    FOR EACH ROW
    EXECUTE FUNCTION recycle_user_account();
