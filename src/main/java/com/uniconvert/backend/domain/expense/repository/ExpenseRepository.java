package com.uniconvert.backend.domain.expense.repository;

import com.uniconvert.backend.domain.expense.entity.Expense;
import com.uniconvert.backend.domain.report.dto.response.CategoryAmount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.Modifying;
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


    // ★ Report 도메인에서 사용 — 기간 내 지출 원본 리스트 (날짜별 집계는 서비스 레이어에서 처리)
    @Query("""
            SELECT e FROM Expense e
            WHERE e.user.id = :userId
              AND e.deletedAt IS NULL
              AND e.spentAt >= :startAt
              AND e.spentAt <= :endAt
            """)
    List<Expense> findAllInPeriod(@Param("userId") Long userId,
                                  @Param("startAt") LocalDateTime startAt,
                                  @Param("endAt") LocalDateTime endAt);

    // ★ Report 도메인에서 사용 — 기간 내 카테고리별 지출 합계
    @Query("""
            SELECT new com.uniconvert.backend.domain.report.dto.response.CategoryAmount(
                e.categoryId, SUM(e.convertedAmountHome))
            FROM Expense e
            WHERE e.user.id = :userId
              AND e.deletedAt IS NULL
              AND e.spentAt >= :startAt
              AND e.spentAt <= :endAt
            GROUP BY e.categoryId
            """)
    List<CategoryAmount> findCategoryAmounts(@Param("userId") Long userId,
                                             @Param("startAt") LocalDateTime startAt,
                                             @Param("endAt") LocalDateTime endAt);
    // 메모 모아보기 — 메모 있는 지출만, 키워드 검색 가능 (정렬은 Pageable로 전달)
    @Query("""
        SELECT e FROM Expense e
        WHERE e.user.id = :userId
          AND e.deletedAt IS NULL
          AND e.memo IS NOT NULL
          AND (:keyword IS NULL OR e.memo LIKE CONCAT('%', :keyword, '%'))
        """)
    Page<Expense> findMemosByFilter(@Param("userId") Long userId,
                                    @Param("keyword") String keyword,
                                    Pageable pageable);

    // 메모 다중 삭제 — 선택한 id 중 본인 소유인 것만 memo를 null로
    @Modifying
    @Query("""
        UPDATE Expense e SET e.memo = null
        WHERE e.id IN :expenseIds AND e.user.id = :userId
        """)
    int clearMemosByIdsAndUserId(@Param("expenseIds") List<Long> expenseIds,
                                 @Param("userId") Long userId);

    // 삭제 가능한(메모 있는) 지출이 몇 건인지 사전 확인용
    @Query("""
        SELECT COUNT(e) FROM Expense e
        WHERE e.id IN :expenseIds AND e.user.id = :userId
          AND e.memo IS NOT NULL AND e.deletedAt IS NULL
        """)
    long countDeletableMemos(@Param("expenseIds") List<Long> expenseIds, @Param("userId") Long userId);
}