package com.uniconvert.backend.domain.currency.controller;

import com.uniconvert.backend.domain.currency.dto.response.CurrencyResponse;
import com.uniconvert.backend.domain.currency.service.CurrencyService;
import com.uniconvert.backend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reference & Budget", description = "참조 데이터 및 예산 관련 API")
public class CurrencyController {

    private final CurrencyService currencyService;

    @Operation(summary = "통화 목록 조회 | 텐텐", description = "온보딩 및 지출 입력에서 사용할 수 있는 통화 목록을 조회합니다.")
    @GetMapping("/currencies")
    public ApiResponse<List<CurrencyResponse>> getCurrencies() {
        return ApiResponse.success(currencyService.getCurrencies());
    }
}