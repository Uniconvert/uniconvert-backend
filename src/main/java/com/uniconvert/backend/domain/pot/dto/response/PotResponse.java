package com.uniconvert.backend.domain.pot.dto.response;

import com.uniconvert.backend.domain.pot.entity.Pot;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Pot 정보 응답")
public record PotResponse(

        @Schema(
                description = "Pot 고유 ID",
                example = "1"
        )
        Long potId,

        @Schema(
                description = "Pot 이름",
                example = "유럽 여행"
        )
        String name,

        @Schema(
                description = "Pot의 목표 대표 카테고리",
                example = "TRAVEL"
        )
        String goalCategory,

        @Schema(
                description = "Pot의 최종 목표 금액",
                example = "3000000"
        )
        BigDecimal targetAmount,

        @Schema(
                description = """
                        전체 기간 동안 Pot에 실제로 배정된 누적 금액입니다.
                        월별 Pot 배정 금액의 누적 결과입니다.
                        """,
                example = "900000"
        )
        BigDecimal savedAmount,

        @Schema(
                description = """
                        Pot 생성 또는 수정 시 설정한 월 저축 계획 금액입니다.
                        실제 월별 배정 금액과는 별개의 계획값입니다.
                        """,
                example = "300000"
        )
        BigDecimal monthlyAllocation,

        @Schema(
                description = """
                        사용자 시간대를 기준으로 이번 달에 실제 배정된 금액입니다.
                        monthlyAllocation은 계획 금액이고,
                        thisMonthAmount는 해당 월에 실제 저장된 배정 금액입니다.
                        """,
                example = "250000"
        )
        BigDecimal thisMonthAmount,

        @Schema(
                description = "Pot 보관 여부. true이면 보관된 Pot입니다.",
                example = "false"
        )
        boolean archived,

        @Schema(
                description = "Pot 목록 화면의 표시 순서",
                example = "1"
        )
        Long displayOrder,

        @Schema(
                description = "Pot 생성 일시",
                example = "2026-08-04T12:00:00"
        )
        LocalDateTime createdAt,

        @Schema(
                description = "Pot 마지막 수정 일시",
                example = "2026-08-04T12:10:00"
        )
        LocalDateTime updatedAt
) {

    /**
     * 생성·수정 API처럼 이번 달 금액을 별도로 조회하지 않는 곳에서 사용합니다.
     *
     * 기존 PotResponse.from(pot) 호출이 깨지지 않도록 유지합니다.
     */
    public static PotResponse from(Pot pot) {
        return from(pot, BigDecimal.ZERO);
    }

    /**
     * Pot 목록·상세 조회에서 이번 달 배정 금액까지 함께 반환합니다.
     */
    public static PotResponse from(
            Pot pot,
            BigDecimal thisMonthAmount
    ) {
        return new PotResponse(
                pot.getId(),
                pot.getName(),
                pot.getGoalCategory(),
                pot.getTargetAmount(),
                defaultZero(pot.getSavedAmount()),
                defaultZero(pot.getMonthlyAllocation()),
                defaultZero(thisMonthAmount),
                pot.isArchived(),
                pot.getDisplayOrder(),
                pot.getCreatedAt(),
                pot.getUpdatedAt()
        );
    }

    private static BigDecimal defaultZero(BigDecimal amount) {
        return amount != null
                ? amount
                : BigDecimal.ZERO;
    }
}