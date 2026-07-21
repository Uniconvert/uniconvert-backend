package com.uniconvert.backend.domain.auth.repository;

import com.uniconvert.backend.domain.auth.entity.AuthIdentity;
import com.uniconvert.backend.domain.auth.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthIdentityRepository extends JpaRepository<AuthIdentity, Long> {

    Optional<AuthIdentity> findByProviderAndProviderUserId(
            AuthProvider provider,
            String providerUserId
    );
}