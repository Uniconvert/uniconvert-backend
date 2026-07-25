DROP TABLE IF EXISTS wallet_transfer;
DROP TABLE IF EXISTS main_wallet_transaction;
DROP TABLE IF EXISTS wallet_transaction;
DROP TABLE IF EXISTS sub_wallet;
DROP TABLE IF EXISTS main_wallet;
DROP TABLE IF EXISTS wallet;
DROP TABLE IF EXISTS pot_allocation;
DROP TABLE IF EXISTS pot;

CREATE TABLE pot (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    name                VARCHAR(255) NOT NULL,
    goal_category       VARCHAR(30) NULL,
    target_amount       DECIMAL(19,4) NOT NULL,
    saved_amount        DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    monthly_allocation  DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    is_archived         BOOLEAN NOT NULL DEFAULT FALSE,
    display_order       BIGINT NOT NULL DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pot_allocation (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    pot_id              BIGINT NOT NULL,
    `year_month`        VARCHAR(7) NOT NULL,
    amount              DECIMAL(19,4) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pot_allocation_pot FOREIGN KEY (pot_id) REFERENCES pot(id)
);

ALTER TABLE expense CHANGE expense_id id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE expense DROP COLUMN wallet_id;
ALTER TABLE expense ADD COLUMN pot_id BIGINT NULL AFTER user_id;
ALTER TABLE expense ADD COLUMN original_currency CHAR(3) NOT NULL AFTER original_amount;
ALTER TABLE expense ADD CONSTRAINT fk_expense_pot FOREIGN KEY (pot_id) REFERENCES pot(id);

ALTER TABLE merchant_category_perference CHANGE merchant_category_perference_id id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE budget CHANGE budget_id id BIGINT NOT NULL AUTO_INCREMENT;
