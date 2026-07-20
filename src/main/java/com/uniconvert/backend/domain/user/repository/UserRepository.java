package com.uniconvert.backend.domain.user.repository;

import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.domain.user.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndUserStatus(String email, UserStatus userStatus);

    boolean existsByEmail(String email);
}