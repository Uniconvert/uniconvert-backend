package com.uniconvert.backend.domain.pot.repository;

import com.uniconvert.backend.domain.pot.entity.PotAllocation;
import com.uniconvert.backend.domain.pot.repository.projection.PotAmountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PotAllocationRepository
        extends JpaRepository<PotAllocation, Long> {

    /**
     * 특정 Pot의 특정 월 배정 정보 조회
     */
    Optional<PotAllocation> findByPot_IdAndYearMonth(
            Long potId,
            String yearMonth
    );

    /**
     * 특정 Pot의 전체 월별 배정 내역 조회
     */
    List<PotAllocation> findAllByPot_IdOrderByYearMonthDesc(
            Long potId
    );

    /**
     * 특정 사용자의 특정 월 PotAllocation 전체 조회
     *
     * 보관된 Pot의 과거 배정 내역도 확인할 수 있도록
     * 여기서는 archived 조건을 걸지 않는다.
     */
    @Query("""
            select pa
            from PotAllocation pa
            join fetch pa.pot p
            where p.user.id = :userId
              and pa.yearMonth = :yearMonth
            order by p.displayOrder asc, p.id asc
            """)
    List<PotAllocation> findAllByUserIdAndYearMonth(
            @Param("userId") Long userId,
            @Param("yearMonth") String yearMonth
    );

    /**
     * 현재 활성화된 Pot의 이번 달 전체 배정 금액 합계
     *
     * 보관된 Pot은 사용 가능 금액 계산에서 제외한다.
     *
     * Pot 보관:
     * archived=true
     * -> 합계에서 제외
     * -> 사용 가능 금액 복구
     *
     * Pot 복구:
     * archived=false
     * -> 합계에 다시 포함
     * -> 사용 가능 금액에서 다시 차감
     */
    @Query("""
            select coalesce(sum(pa.amount), 0)
            from PotAllocation pa
            where pa.pot.user.id = :userId
              and pa.yearMonth = :yearMonth
              and pa.pot.archived = false
            """)
    BigDecimal sumAmountByUserIdAndYearMonth(
            @Param("userId") Long userId,
            @Param("yearMonth") String yearMonth
    );

    /**
     * 특정 사용자의 Pot별 이번 달 실제 배정 금액
     *
     * includeArchived=true 조회에서도 보관 당시 금액을
     * 확인할 수 있어야 하므로 archived 조건은 넣지 않는다.
     */
    @Query("""
            select pa.pot.id as potId,
                   coalesce(sum(pa.amount), 0) as amount
            from PotAllocation pa
            where pa.pot.user.id = :userId
              and pa.yearMonth = :yearMonth
            group by pa.pot.id
            """)
    List<PotAmountProjection> findPotAmountsByUserIdAndYearMonth(
            @Param("userId") Long userId,
            @Param("yearMonth") String yearMonth
    );
}