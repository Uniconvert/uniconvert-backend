package com.uniconvert.backend.domain.expense.controller;

import com.uniconvert.backend.domain.expense.dto.request.ExpenseCreateRequest;
import com.uniconvert.backend.domain.expense.dto.request.ExpenseUpdateRequest;
import com.uniconvert.backend.domain.expense.dto.response.ExpenseImportResponse;
import com.uniconvert.backend.domain.expense.dto.response.ExpenseListItemResponse;
import com.uniconvert.backend.domain.expense.dto.response.ExpenseResponse;
import com.uniconvert.backend.domain.expense.service.ExpenseImportService;
import com.uniconvert.backend.domain.expense.service.ExpenseService;
import com.uniconvert.backend.global.response.ApiResponse;
import com.uniconvert.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final ExpenseImportService expenseImportService;

    @Operation(summary = "지출 등록")
    @PostMapping
    public ApiResponse<ExpenseResponse> createExpense(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ExpenseCreateRequest request
    ) {
        ExpenseResponse response = expenseService.createExpense(userDetails.getUserId(), request);
        return ApiResponse.success(response);
    }

    @Operation(
            summary = "CSV 지출내역 자동 저장",
            description = """
                    Wise 또는 Monzo 계좌 명세서 CSV를 업로드하면 헤더로 형식을 자동 판별해
                    지출성 거래만 골라 저장합니다.

                    - Wise: Amount < 0 이고 State = COMPLETED 인 거래만 저장
                    - Monzo: Money Out이 있거나 Amount < 0인 거래만 저장
                    - 입금·충전·취소 거래는 저장하지 않고 excludedCount에 집계됩니다.
                    - 거래일·통화 기준으로 환율을 조회해 홈 통화 금액으로 저장합니다. DB에 없으면 외부 환율 API를 호출하고,
                      같은 거래일·통화 조합은 한 번만 조회합니다.
                    - 환율을 구하지 못한 행은 저장하지 않고 errors에 사유와 함께 담아 반환합니다(다른 정상 행은 계속 처리).
                    """,
            // Swagger 쪽 RequestBody는 FQN으로 쓴다 — import하면 아래 @RequestBody(Spring)를 가려버려서
            // 요청 본문이 바인딩되지 않고 전부 null이 된다
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
    )
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ExpenseImportResponse> importExpenses(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Wise 또는 Monzo CSV 파일", schema = @Schema(type = "string", format = "binary"))
            @RequestPart("file") MultipartFile file
    ) {
        ExpenseImportResponse response = expenseImportService.importCsv(userDetails.getUserId(), file);
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