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
     *
     * 동일한 pot_id + year_month는 UNIQUE이므로 최대 한 건만 조회된다.
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
     * 특정 사용자의 이번 달 전체 Pot 배정 금액 합계
     *
     * Pots 화면 상단의 'Pots에 배정된 금액'에 사용한다.
     */
    @Query("""
            select coalesce(sum(pa.amount), 0)
            from PotAllocation pa
            where pa.pot.user.id = :userId
              and pa.yearMonth = :yearMonth
            """)
    BigDecimal sumAmountByUserIdAndYearMonth(
            @Param("userId") Long userId,
            @Param("yearMonth") String yearMonth
    );

    /**
     * 특정 사용자의 Pot별 이번 달 배정 금액 조회
     *
     * GET /pots 응답의 각 Pot에 thisMonthAmount를 넣을 때 사용한다.
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