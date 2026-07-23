package com.uniconvert.backend.domain.budget.repository;

import com.uniconvert.backend.domain.budget.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @Query("""
            select b
            from Budget b
            where b.user.id = :userId
              and b.yearMonth = :yearMonth
            """)
    Optional<Budget> findByUserIdAndYearMonth(
            @Param("userId") Long userId,
            @Param("yearMonth") String yearMonth
    );
}