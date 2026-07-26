package com.uniconvert.backend.domain.pot.dto.response;

import com.uniconvert.backend.domain.pot.entity.PotAllocation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PotAllocationResponse(
        Long allocationId,
        Long potId,
        String potName,
        String yearMonth,
        BigDecimal amount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PotAllocationResponse from(
            PotAllocation allocation
    ) {
        return new PotAllocationResponse(
                allocation.getId(),
                allocation.getPot().getId(),
                allocation.getPot().getName(),
                allocation.getYearMonth(),
                allocation.getAmount(),
                allocation.getCreatedAt(),
                allocation.getUpdatedAt()
        );
    }
}