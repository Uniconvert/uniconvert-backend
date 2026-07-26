package com.uniconvert.backend.domain.pot.repository;

import com.uniconvert.backend.domain.pot.entity.PotAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PotAllocationRepository
        extends JpaRepository<PotAllocation, Long> {

    Optional<PotAllocation> findByPot_IdAndYearMonth(
            Long potId,
            String yearMonth
    );

    List<PotAllocation> findAllByPot_IdOrderByYearMonthDesc(
            Long potId
    );

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
}