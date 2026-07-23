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

import com.uniconvert.backend.domain.exchange.dto.response.QuoteHistoryResponse;
import com.uniconvert.backend.domain.exchange.entity.QuoteHistory;
import com.uniconvert.backend.global.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.data.domain.PageRequest;

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
        ExchangeRateResponse response = exchangeRateService.getCurrentRateWithChange(from);
        return ApiResponse.success(response);
    }

    @Operation(summary = "환율 계산기")
    @GetMapping("/quote")
    public ApiResponse<ExchangeQuoteResponse> quote(
            @AuthenticationPrincipal CustomUserDetails userDetails,
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

        exchangeRateService.saveQuoteHistory(userDetails.getUserId(), from, to, amount, converted, rate.getRate());

        return ApiResponse.success(new ExchangeQuoteResponse(
                from, to, amount, rate.getRate(), converted, rate.getRateDate()
        ));
    }
    @Operation(
            summary = "최근 계산 내역 조회",
            description = """
                환율 계산기(quote)로 계산했던 내역을 최신순으로 조회합니다.

                page: 조회할 페이지 번호 (0부터 시작, 기본값 0)
                size: 한 페이지에 담을 개수 (기본값 10)
                """
    )
    @GetMapping("/quote/history")
    public ApiResponse<Page<QuoteHistoryResponse>> getQuoteHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuoteHistory> history = exchangeRateService.getQuoteHistory(userDetails.getUserId(), pageable);
        return ApiResponse.success(history.map(QuoteHistoryResponse::from));
    }

}