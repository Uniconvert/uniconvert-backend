package com.uniconvert.backend.domain.report.controller;

import com.uniconvert.backend.domain.report.dto.response.ReportCategoriesResponse;
import com.uniconvert.backend.domain.report.dto.response.ReportSummaryResponse;
import com.uniconvert.backend.domain.report.service.ReportService;
import com.uniconvert.backend.global.response.ApiResponse;
import com.uniconvert.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uniconvert.backend.domain.report.dto.response.ReportMonthlyResponse;
import java.time.YearMonth;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "리포트", description = "지출 요약·카테고리별 리포트")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "기간별 지출 요약", description = "일별 지출 합계 리스트. 주간·월간 막대그래프 공용 (기간만 다르게 호출)")
    @GetMapping("/summary")
    public ApiResponse<ReportSummaryResponse> getSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return ApiResponse.success(reportService.getSummary(userDetails.getUserId(), startDate, endDate));
    }

    @Operation(summary = "카테고리별 지출 리포트")
    @GetMapping("/categories")
    public ApiResponse<ReportCategoriesResponse> getCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return ApiResponse.success(reportService.getCategoryReport(userDetails.getUserId(), startDate, endDate));
    }

    @Operation(summary = "월별 지출 요약", description = "기준월(endMonth)부터 과거 months개월치 지출 합계. 기본값: 이번 달 기준 7개월")
    @GetMapping("/monthly")
    public ApiResponse<ReportMonthlyResponse> getMonthly(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String endMonth,
            @RequestParam(defaultValue = "7") int months
    ) {
        YearMonth resolvedEndMonth = (endMonth != null) ? YearMonth.parse(endMonth) : YearMonth.now();
        ReportMonthlyResponse response =
                reportService.getMonthlySummary(userDetails.getUserId(), resolvedEndMonth, months);
        return ApiResponse.success(response);
    }

}