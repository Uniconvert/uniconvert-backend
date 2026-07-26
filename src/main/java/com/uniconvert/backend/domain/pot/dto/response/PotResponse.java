package com.uniconvert.backend.domain.pot.dto.response;

import com.uniconvert.backend.domain.pot.entity.Pot;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PotResponse(
        Long potId,
        String name,
        String goalCategory,
        BigDecimal targetAmount,
        BigDecimal savedAmount,
        BigDecimal monthlyAllocation,
        boolean archived,
        Long displayOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PotResponse from(Pot pot) {
        return new PotResponse(
                pot.getId(),
                pot.getName(),
                pot.getGoalCategory(),
                pot.getTargetAmount(),
                pot.getSavedAmount(),
                pot.getMonthlyAllocation(),
                pot.isArchived(),
                pot.getDisplayOrder(),
                pot.getCreatedAt(),
                pot.getUpdatedAt()
        );
    }
}