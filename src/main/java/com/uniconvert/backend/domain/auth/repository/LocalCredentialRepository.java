package com.uniconvert.backend.domain.auth.repository;

import com.uniconvert.backend.domain.auth.entity.LocalCredential;
import com.uniconvert.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocalCredentialRepository extends JpaRepository<LocalCredential, Long> {

    Optional<LocalCredential> findByUser(User user);
}