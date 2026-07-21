package com.uniconvert.backend.domain.auth.repository;

import com.uniconvert.backend.domain.auth.entity.RefreshToken;
import com.uniconvert.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHashAndRevokedAtIsNull(String tokenHash);

    void deleteByUser(User user);
}
