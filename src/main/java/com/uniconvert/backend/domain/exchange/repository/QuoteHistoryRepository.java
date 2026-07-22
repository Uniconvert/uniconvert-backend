package com.uniconvert.backend.domain.exchange.repository;

import com.uniconvert.backend.domain.exchange.entity.QuoteHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteHistoryRepository extends JpaRepository<QuoteHistory, Long> {
    Page<QuoteHistory> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}