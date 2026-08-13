package com.uniconvert.backend.domain.exchange.dto.response;

import com.uniconvert.backend.global.uni.dto.UniMessageBundleResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "계산기 현재 환율 및 유니 메시지 응답")
public record CalculatorRateResponse(

        String fromCurrency,
        String toCurrency,
        BigDecimal rate,
        LocalDate rateDate,
        BigDecimal changeRate,
        LocalDate comparedDate,

        @Schema(description = "계산기 화면 유니 메시지")
        UniMessageBundleResponse uniMessages

) {
}
