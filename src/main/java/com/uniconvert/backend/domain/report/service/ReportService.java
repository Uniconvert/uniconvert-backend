package com.uniconvert.backend.domain.report.service;

import com.uniconvert.backend.domain.category.enums.CategoryType;
import com.uniconvert.backend.domain.expense.repository.ExpenseRepository;
import com.uniconvert.backend.domain.report.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.uniconvert.backend.domain.expense.entity.Expense;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ExpenseRepository expenseRepository;

    // 일별 지출 요약 (주간/월간 막대그래프 공용) — 빈 날짜는 0으로 채워서 반환
    @Transactional(readOnly = true)
    public ReportSummaryResponse getSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = expenseRepository.findAllInPeriod(
                userId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59)
        );

        // ★ 자바 코드에서 날짜별 합계 계산 (CAST 관련 Hibernate 이슈 회피)
        Map<LocalDate, BigDecimal> amountByDate = expenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getSpentAt().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getConvertedAmountHome, BigDecimal::add)
                ));

        List<DailyAmount> filled = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            BigDecimal amount = amountByDate.getOrDefault(cursor, BigDecimal.ZERO);
            filled.add(new DailyAmount(cursor, amount));
            cursor = cursor.plusDays(1);
        }

        BigDecimal total = filled.stream()
                .map(DailyAmount::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ChangeInfo change = calculateTodayChange(userId);

        return new ReportSummaryResponse(total, filled, change.changeRate(), change.comparedDate());
    }

    // ★ 추가: 오늘 대비 어제 지출 변동률
    // 요청 기간(startDate~endDate)과 무관하게 항상 "실제 오늘/어제" 기준으로 계산
    private ChangeInfo calculateTodayChange(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        BigDecimal todayTotal = expenseRepository.sumConvertedAmountByPeriod(
                userId, today.atStartOfDay(), today.atTime(23, 59, 59));
        BigDecimal yesterdayTotal = expenseRepository.sumConvertedAmountByPeriod(
                userId, yesterday.atStartOfDay(), yesterday.atTime(23, 59, 59));

        // 어제 지출이 0원이면 나눗셈 불가 → 비교 포기 (환율 changeRate 로직과 동일 패턴)
        if (yesterdayTotal == null || yesterdayTotal.compareTo(BigDecimal.ZERO) == 0) {
            return new ChangeInfo(null, null);
        }

        BigDecimal changeRate = todayTotal.subtract(yesterdayTotal)
                .divide(yesterdayTotal, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        return new ChangeInfo(changeRate, yesterday);
    }

    private record ChangeInfo(BigDecimal changeRate, LocalDate comparedDate) {}

    // 카테고리별 지출 요약 (변경 없음)
    @Transactional(readOnly = true)
    public ReportCategoriesResponse getCategoryReport(Long userId, LocalDate startDate, LocalDate endDate) {
        List<CategoryAmount> categoryAmounts = expenseRepository.findCategoryAmounts(
                userId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59)
        );

        BigDecimal total = categoryAmounts.stream()
                .map(CategoryAmount::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ReportCategoryItem> items = categoryAmounts.stream()
                .map(ca -> {
                    CategoryType type = CategoryType.fromId(ca.categoryId());
                    BigDecimal percentage = total.compareTo(BigDecimal.ZERO) > 0
                            ? ca.amount().divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                            : BigDecimal.ZERO;
                    return new ReportCategoryItem(
                            ca.categoryId(),
                            type != null ? type.getDisplayName() : null,
                            type != null ? type.getIconKey() : null,
                            ca.amount(),
                            percentage.setScale(1, RoundingMode.HALF_UP)
                    );
                })
                .toList();

        return new ReportCategoriesResponse(total, items);
    }
}