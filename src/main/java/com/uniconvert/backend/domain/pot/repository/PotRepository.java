package com.uniconvert.backend.domain.pot.repository;

import com.uniconvert.backend.domain.pot.entity.Pot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PotRepository extends JpaRepository<Pot, Long> {

    Optional<Pot> findByIdAndUser_Id(
            Long potId,
            Long userId
    );

    List<Pot> findAllByUser_IdAndArchivedFalseOrderByDisplayOrderAscIdAsc(
            Long userId
    );

    List<Pot> findAllByUser_IdOrderByDisplayOrderAscIdAsc(
            Long userId
    );

    @Query("""
            select coalesce(max(p.displayOrder), 0)
            from Pot p
            where p.user.id = :userId
            """)
    Long findMaxDisplayOrderByUserId(
            @Param("userId") Long userId
    );
}