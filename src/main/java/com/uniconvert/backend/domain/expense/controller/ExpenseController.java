package com.uniconvert.backend.domain.expense.controller;

import com.uniconvert.backend.domain.expense.dto.request.ExpenseCreateRequest;
import com.uniconvert.backend.domain.expense.dto.request.ExpenseUpdateRequest;
import com.uniconvert.backend.domain.expense.dto.response.ExpenseListItemResponse;
import com.uniconvert.backend.domain.expense.dto.response.ExpenseResponse;
import com.uniconvert.backend.domain.expense.service.ExpenseService;
import com.uniconvert.backend.global.response.ApiResponse;
import com.uniconvert.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
@Tag(name = "지출", description = "지출 등록·조회·수정·삭제")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "지출 등록")
    @PostMapping
    public ApiResponse<ExpenseResponse> createExpense(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ExpenseCreateRequest request
    ) {
        ExpenseResponse response = expenseService.createExpense(userDetails.getUserId(), request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "지출 단건 조회")
    @GetMapping("/{expenseId}")
    public ApiResponse<ExpenseResponse> getExpense(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long expenseId
    ) {
        ExpenseResponse response = expenseService.getExpense(userDetails.getUserId(), expenseId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "지출 목록 조회", description = "필터: 기간(startAt~endAt), 카테고리. page=0부터, size 기본 6")
    @GetMapping
    public ApiResponse<Page<ExpenseListItemResponse>> getExpenses(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) LocalDateTime startAt,
            @RequestParam(required = false) LocalDateTime endAt,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, 6);
        Page<ExpenseListItemResponse> response =
                expenseService.getExpenses(userDetails.getUserId(), startAt, endAt, categoryId, pageable);
        return ApiResponse.success(response);
    }

    @Operation(summary = "최근 지출 조회", description = "홈 화면 카드용")
    @GetMapping("/recent")
    public ApiResponse<List<ExpenseListItemResponse>> getRecentExpenses(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ExpenseListItemResponse> response = expenseService.getRecentExpenses(userDetails.getUserId());
        return ApiResponse.success(response);
    }

    @Operation(summary = "지출 수정")
    @PatchMapping("/{expenseId}")
    public ApiResponse<ExpenseResponse> updateExpense(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long expenseId,
            @Valid @RequestBody ExpenseUpdateRequest request
    ) {
        ExpenseResponse response = expenseService.updateExpense(userDetails.getUserId(), expenseId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "지출 삭제 (소프트 삭제)")
    @DeleteMapping("/{expenseId}")
    public ApiResponse<Void> deleteExpense(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long expenseId
    ) {
        expenseService.deleteExpense(userDetails.getUserId(), expenseId);
        return ApiResponse.success(null);
    }

    @Operation(summary = "남은 예산 조회", description = "예산 - Pot 배정 합계 - 지출 합계 (Pot 연동 전까지는 지출만 반영)")
    @GetMapping("/remaining-budget")
    public ApiResponse<BigDecimal> getRemainingBudget(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String yearMonth
    ) {
        BigDecimal remaining = expenseService.getRemainingBudget(userDetails.getUserId(), YearMonth.parse(yearMonth));
        return ApiResponse.success(remaining);
    }
}