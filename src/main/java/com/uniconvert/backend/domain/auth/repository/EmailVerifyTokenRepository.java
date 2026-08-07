package com.uniconvert.backend.domain.auth.repository;

import com.uniconvert.backend.domain.auth.entity.EmailVerifyToken;
import com.uniconvert.backend.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerifyTokenRepository extends JpaRepository<EmailVerifyToken, Long> {
    Optional<EmailVerifyToken> findByUser(User user);
    Optional<EmailVerifyToken> findByTokenHash(String tokenHash);
}
