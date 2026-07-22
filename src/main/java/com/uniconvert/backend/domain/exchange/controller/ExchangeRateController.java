package com.uniconvert.backend.domain.exchange.controller;

import com.uniconvert.backend.domain.exchange.dto.response.ExchangeQuoteResponse;
import com.uniconvert.backend.domain.exchange.dto.response.ExchangeRateResponse;
import com.uniconvert.backend.domain.exchange.entity.DailyExchangeRate;
import com.uniconvert.backend.domain.exchange.service.ExchangeRateService;
import com.uniconvert.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@RestController
@RequestMapping("/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @Operation(summary = "현재 대표 환율 조회")
    @GetMapping("/current")
    public ApiResponse<ExchangeRateResponse> getCurrent(@RequestParam String from, @RequestParam String to) {
        DailyExchangeRate rate = exchangeRateService.getCurrentRate(from);
        return ApiResponse.success(ExchangeRateResponse.from(rate));
    }

    @Operation(summary = "환율 계산기")
    @GetMapping("/quote")
    public ApiResponse<ExchangeQuoteResponse> quote(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount,
            @Parameter(description = """
        조회할 환율 기준 날짜 (형식: YYYY-MM-DD, 예: 2026-07-15)

        미입력 시 오늘(또는 가장 최근 영업일) 환율이 적용됩니다.

        주말·공휴일 등 비영업일을 입력하면, 해당 날짜가 아닌
        가장 최근 영업일의 환율값이 반환됩니다.
        응답의 rateDate 필드로 실제 적용된 날짜를 확인할 수 있습니다.
        """)
            @RequestParam(required = false) LocalDate date
    ) {
        DailyExchangeRate rate = (date != null)
                ? exchangeRateService.getRateByDate(from, date)
                : exchangeRateService.getCurrentRate(from);
        BigDecimal converted = amount.multiply(rate.getRate()).setScale(2, RoundingMode.HALF_UP);
        return ApiResponse.success(new ExchangeQuoteResponse(
                from, to, amount, rate.getRate(), converted, rate.getRateDate()
        ));
    }
}