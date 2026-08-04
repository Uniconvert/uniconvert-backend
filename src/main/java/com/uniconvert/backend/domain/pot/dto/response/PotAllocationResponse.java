package com.uniconvert.backend.domain.pot.dto.response;

import com.uniconvert.backend.domain.pot.entity.PotAllocation;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Pot 월별 실제 배정 내역 응답")
public record PotAllocationResponse(

        @Schema(
                description = "월별 Pot 배정 내역의 고유 ID",
                example = "10"
        )
        Long allocationId,

        @Schema(
                description = "금액이 배정된 Pot의 고유 ID",
                example = "1"
        )
        Long potId,

        @Schema(
                description = "금액이 배정된 Pot 이름",
                example = "유럽 여행"
        )
        String potName,

        @Schema(
                description = "배정 대상 월. YYYY-MM 형식",
                example = "2026-08"
        )
        String yearMonth,

        @Schema(
                description = """
                        해당 월에 Pot에 실제로 배정된 금액입니다.
                        월 저축 계획 금액인 monthlyAllocation과는 별개의 실제 배정값입니다.
                        """,
                example = "300000"
        )
        BigDecimal amount,

        @Schema(
                description = "월별 배정 내역 생성 일시",
                example = "2026-08-04T12:00:00"
        )
        LocalDateTime createdAt,

        @Schema(
                description = "월별 배정 금액의 마지막 수정 일시",
                example = "2026-08-04T12:10:00"
        )
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