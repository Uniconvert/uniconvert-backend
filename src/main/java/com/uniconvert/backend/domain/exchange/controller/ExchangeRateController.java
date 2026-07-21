package com.uniconvert.backend.domain.exchange.controller;

import com.uniconvert.backend.domain.exchange.dto.response.ExchangeQuoteResponse;
import com.uniconvert.backend.domain.exchange.dto.response.ExchangeRateResponse;
import com.uniconvert.backend.domain.exchange.entity.DailyExchangeRate;
import com.uniconvert.backend.domain.exchange.service.ExchangeRateService;
import com.uniconvert.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
            @RequestParam BigDecimal amount
    ) {
        DailyExchangeRate rate = exchangeRateService.getCurrentRate(from);
        BigDecimal converted = amount.multiply(rate.getRate()).setScale(2, RoundingMode.HALF_UP);
        return ApiResponse.success(new ExchangeQuoteResponse(
                from, to, amount, rate.getRate(), converted, rate.getRateDate()
        ));
    }
}