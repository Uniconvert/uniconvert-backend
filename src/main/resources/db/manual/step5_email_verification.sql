CREATE TABLE IF NOT EXISTS `email_verify_token` (
    `email_verify_token_id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `token_hash` CHAR(64) NOT NULL,
    `expires_at` DATETIME(6) NOT NULL,
    `resend_count` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`email_verify_token_id`),
    UNIQUE KEY `uk_email_verify_token_user` (`user_id`),
    UNIQUE KEY `uk_email_verify_token_hash` (`token_hash`),
    CONSTRAINT `fk_email_verify_token_user`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
