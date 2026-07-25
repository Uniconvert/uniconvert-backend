package com.uniconvert.backend.domain.expense.repository;

import com.uniconvert.backend.domain.expense.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // 단건 조회 — 본인 지출만, 삭제된 건 제외
    Optional<Expense> findByIdAndUser_IdAndDeletedAtIsNull(Long id, Long userId);

    // 목록 조회 (필터: 기간·카테고리 optional) — page=0&size=6
    @Query("""
            SELECT e FROM Expense e
            WHERE e.user.id = :userId
              AND e.deletedAt IS NULL
              AND (:startAt IS NULL OR e.spentAt >= :startAt)
              AND (:endAt IS NULL OR e.spentAt <= :endAt)
              AND (:categoryId IS NULL OR e.categoryId = :categoryId)
            ORDER BY e.spentAt DESC
            """)
    Page<Expense> findAllByFilter(@Param("userId") Long userId,
                                  @Param("startAt") LocalDateTime startAt,
                                  @Param("endAt") LocalDateTime endAt,
                                  @Param("categoryId") Long categoryId,
                                  Pageable pageable);

    // 남은 예산 계산용 — 해당 기간 지출 총합(홈 통화 기준)
    @Query("""
            SELECT COALESCE(SUM(e.convertedAmountHome), 0)
            FROM Expense e
            WHERE e.user.id = :userId
              AND e.deletedAt IS NULL
              AND e.spentAt >= :startAt
              AND e.spentAt <= :endAt
            """)
    BigDecimal sumConvertedAmountByPeriod(@Param("userId") Long userId,
                                          @Param("startAt") LocalDateTime startAt,
                                          @Param("endAt") LocalDateTime endAt);

    // 최근 지출 (홈 화면 "최근 지출" 카드용, 2건 정도)
    List<Expense> findTop5ByUser_IdAndDeletedAtIsNullOrderBySpentAtDesc(Long userId);
}