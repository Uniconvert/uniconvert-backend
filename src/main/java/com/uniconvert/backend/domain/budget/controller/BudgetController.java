package com.uniconvert.backend.domain.budget.controller;

import com.uniconvert.backend.domain.budget.dto.request.BudgetUpsertRequest;
import com.uniconvert.backend.domain.budget.dto.response.BudgetResponse;
import com.uniconvert.backend.domain.budget.service.BudgetService;
import com.uniconvert.backend.global.response.ApiResponse;
import com.uniconvert.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor

@Tag(name = "Reference & Budget", description = "참조 데이터 및 예산 관련 API")
public class BudgetController {

    private final BudgetService budgetService;

    @Operation(summary = "월 예산 조회", description = "특정 월의 홈 통화 기준 예산을 조회한다.")
    @GetMapping("/{yearMonth}")
    public ApiResponse<BudgetResponse> getBudget(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String yearMonth
    ) {
        return ApiResponse.success(budgetService.getBudget(userDetails.getUserId(), yearMonth));
    }

    @Operation(summary = "월 예산 생성·수정", description = "해당 월 예산을 생성하거나 수정한다(upsert).")
    @PutMapping("/{yearMonth}")
    public ApiResponse<BudgetResponse> upsertBudget(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String yearMonth,
            @Valid @RequestBody BudgetUpsertRequest request
    ) {
        return ApiResponse.success(budgetService.upsertBudget(userDetails.getUserId(), yearMonth, request));
    }
}