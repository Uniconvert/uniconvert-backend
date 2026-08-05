package com.uniconvert.backend.domain.report.service;

import com.uniconvert.backend.domain.category.enums.CategoryType;
import com.uniconvert.backend.domain.expense.entity.Expense;
import com.uniconvert.backend.domain.expense.repository.ExpenseRepository;
import com.uniconvert.backend.domain.report.dto.response.CategoryAmount;
import com.uniconvert.backend.domain.report.dto.response.DailyAmount;
import com.uniconvert.backend.domain.report.dto.response.ReportCategoriesResponse;
import com.uniconvert.backend.domain.report.dto.response.ReportCategoryItem;
import com.uniconvert.backend.domain.report.dto.response.ReportSummaryResponse;
import com.uniconvert.backend.global.uni.dto.UniMessageBundleResponse;
import com.uniconvert.backend.global.uni.dto.UniMessageResponse;
import com.uniconvert.backend.global.uni.enums.UniSection;
import com.uniconvert.backend.global.uni.service.UniInsightMessageFactory;
import com.uniconvert.backend.global.uni.service.UniMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final DateTimeFormatter MONTH_DAY_FORMATTER =
            DateTimeFormatter.ofPattern("M/d");

    private final ExpenseRepository expenseRepository;
    private final UniInsightMessageFactory uniInsightMessageFactory;
    private final UniMessageService uniMessageService;

    /**
     * 일별 지출 요약
     *
     * 주간 및 월간 막대그래프에서 공통으로 사용하며,
     * 지출이 없는 날짜도 0원으로 채워서 반환합니다.
     */
    @Transactional(readOnly = true)
    public ReportSummaryResponse getSummary(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateDateRange(startDate, endDate);

        List<Expense> expenses =
                expenseRepository.findAllInPeriod(
                        userId,
                        startDate.atStartOfDay(),
                        endDate.atTime(23, 59, 59)
                );

        Map<LocalDate, BigDecimal> amountByDate =
                createAmountByDate(expenses);

        List<DailyAmount> dailyAmounts =
                fillDailyAmounts(
                        startDate,
                        endDate,
                        amountByDate
                );

        BigDecimal totalAmount = dailyAmounts.stream()
                .map(DailyAmount::amount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        ChangeInfo changeInfo =
                calculateTodayChange(userId);

        String weeklyMaxDate =
                calculateWeeklyMaxDate(userId);

        String topCategory =
                calculateMonthlyTopCategory(userId);

        List<UniMessageResponse> insights =
                uniInsightMessageFactory.createReportInsights(
                        changeInfo.changeRate(),
                        weeklyMaxDate
                );

        UniMessageBundleResponse uniMessages =
                uniMessageService.createBundle(
                        UniSection.REPORT,
                        insights
                );

        return new ReportSummaryResponse(
                totalAmount,
                dailyAmounts,
                changeInfo.changeRate(),
                changeInfo.comparedDate(),
                weeklyMaxDate,
                changeInfo.todayExpense(),
                topCategory,
                uniMessages
        );
    }

    /**
     * 오늘 지출과 어제 지출을 비교합니다.
     *
     * 어제 지출이 0원이면 변동률을 계산할 수 없으므로
     * changeRate와 comparedDate는 null로 반환합니다.
     * todayExpense는 비교 가능 여부와 관계없이 반환합니다.
     */
    private ChangeInfo calculateTodayChange(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        BigDecimal todayTotal = defaultZero(
                expenseRepository.sumConvertedAmountByPeriod(
                        userId,
                        today.atStartOfDay(),
                        today.atTime(23, 59, 59)
                )
        );

        BigDecimal yesterdayTotal = defaultZero(
                expenseRepository.sumConvertedAmountByPeriod(
                        userId,
                        yesterday.atStartOfDay(),
                        yesterday.atTime(23, 59, 59)
                )
        );

        if (yesterdayTotal.compareTo(BigDecimal.ZERO) == 0) {
            return new ChangeInfo(
                    null,
                    null,
                    todayTotal
            );
        }

        BigDecimal changeRate = todayTotal
                .subtract(yesterdayTotal)
                .divide(
                        yesterdayTotal,
                        6,
                        RoundingMode.HALF_UP
                )
                .multiply(BigDecimal.valueOf(100))
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );

        return new ChangeInfo(
                changeRate,
                yesterday,
                todayTotal
        );
    }

    /**
     * 이번 주 월요일부터 오늘까지의 지출 중
     * 가장 많은 금액을 지출한 날짜를 반환합니다.
     *
     * 동일한 금액의 날짜가 여러 개이면 더 빠른 날짜를 선택합니다.
     */
    private String calculateWeeklyMaxDate(Long userId) {
        LocalDate today = LocalDate.now();

        LocalDate weekStart = today.with(
                TemporalAdjusters.previousOrSame(
                        DayOfWeek.MONDAY
                )
        );

        List<Expense> weeklyExpenses =
                expenseRepository.findAllInPeriod(
                        userId,
                        weekStart.atStartOfDay(),
                        today.atTime(23, 59, 59)
                );

        if (weeklyExpenses.isEmpty()) {
            return null;
        }

        Map<LocalDate, BigDecimal> amountByDate =
                createAmountByDate(weeklyExpenses);

        return amountByDate.entrySet()
                .stream()
                .filter(entry ->
                        entry.getValue()
                                .compareTo(BigDecimal.ZERO) > 0
                )
                .sorted(
                        Map.Entry
                                .<LocalDate, BigDecimal>comparingByValue()
                                .reversed()
                                .thenComparing(Map.Entry.comparingByKey())
                )
                .map(Map.Entry::getKey)
                .findFirst()
                .map(date ->
                        date.format(MONTH_DAY_FORMATTER)
                )
                .orElse(null);
    }

    /**
     * 이번 달 카테고리별 지출 합계를 비교해
     * 가장 많이 지출한 카테고리명을 반환합니다.
     */
    private String calculateMonthlyTopCategory(Long userId) {
        YearMonth currentMonth =
                YearMonth.now();

        LocalDateTime startAt =
                currentMonth.atDay(1)
                        .atStartOfDay();

        LocalDateTime endAt =
                currentMonth.atEndOfMonth()
                        .atTime(23, 59, 59);

        List<CategoryAmount> categoryAmounts =
                expenseRepository.findCategoryAmounts(
                        userId,
                        startAt,
                        endAt
                );

        return categoryAmounts.stream()
                .filter(categoryAmount ->
                        categoryAmount.amount() != null
                                && categoryAmount.amount()
                                .compareTo(BigDecimal.ZERO) > 0
                )
                .max(
                        Comparator.comparing(
                                CategoryAmount::amount
                        )
                )
                .map(CategoryAmount::categoryId)
                .map(CategoryType::fromId)
                .map(CategoryType::getDisplayName)
                .orElse(null);
    }

    /**
     * 날짜별 지출 합계를 계산합니다.
     */
    private Map<LocalDate, BigDecimal> createAmountByDate(
            List<Expense> expenses
    ) {
        return expenses.stream()
                .collect(
                        Collectors.groupingBy(
                                expense ->
                                        expense.getSpentAt()
                                                .toLocalDate(),
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        Expense::getConvertedAmountHome,
                                        BigDecimal::add
                                )
                        )
                );
    }

    /**
     * 조회 기간 중 지출이 없는 날짜도 0원으로 채웁니다.
     */
    private List<DailyAmount> fillDailyAmounts(
            LocalDate startDate,
            LocalDate endDate,
            Map<LocalDate, BigDecimal> amountByDate
    ) {
        List<DailyAmount> dailyAmounts =
                new ArrayList<>();

        LocalDate cursor = startDate;

        while (!cursor.isAfter(endDate)) {
            BigDecimal amount =
                    amountByDate.getOrDefault(
                            cursor,
                            BigDecimal.ZERO
                    );

            dailyAmounts.add(
                    new DailyAmount(
                            cursor,
                            amount
                    )
            );

            cursor = cursor.plusDays(1);
        }

        return dailyAmounts;
    }

    private void validateDateRange(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "조회 시작일과 종료일은 필수입니다."
            );
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "조회 시작일은 종료일보다 늦을 수 없습니다."
            );
        }
    }

    private BigDecimal defaultZero(
            BigDecimal amount
    ) {
        return amount != null
                ? amount
                : BigDecimal.ZERO;
    }

    private record ChangeInfo(
            BigDecimal changeRate,
            LocalDate comparedDate,
            BigDecimal todayExpense
    ) {
    }

    /**
     * 카테고리별 지출 요약
     */
    @Transactional(readOnly = true)
    public ReportCategoriesResponse getCategoryReport(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateDateRange(startDate, endDate);

        List<CategoryAmount> categoryAmounts =
                expenseRepository.findCategoryAmounts(
                        userId,
                        startDate.atStartOfDay(),
                        endDate.atTime(23, 59, 59)
                );

        BigDecimal total = categoryAmounts.stream()
                .map(CategoryAmount::amount)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        List<ReportCategoryItem> items =
                categoryAmounts.stream()
                        .map(categoryAmount -> {
                            CategoryType type =
                                    CategoryType.fromId(
                                            categoryAmount.categoryId()
                                    );

                            BigDecimal percentage =
                                    total.compareTo(BigDecimal.ZERO) > 0
                                            ? categoryAmount.amount()
                                            .divide(
                                                    total,
                                                    4,
                                                    RoundingMode.HALF_UP
                                            )
                                            .multiply(
                                                    BigDecimal.valueOf(100)
                                            )
                                            : BigDecimal.ZERO;

                            return new ReportCategoryItem(
                                    categoryAmount.categoryId(),
                                    type != null
                                            ? type.getDisplayName()
                                            : null,
                                    type != null
                                            ? type.getIconKey()
                                            : null,
                                    categoryAmount.amount(),
                                    percentage.setScale(
                                            1,
                                            RoundingMode.HALF_UP
                                    )
                            );
                        })
                        .toList();

        return new ReportCategoriesResponse(
                total,
                items
        );
    }
}