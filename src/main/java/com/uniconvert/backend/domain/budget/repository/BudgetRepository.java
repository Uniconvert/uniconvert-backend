package com.uniconvert.backend.domain.budget.repository;

import com.uniconvert.backend.domain.budget.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByUserIdAndYearMonth(Long userId, String yearMonth);
}