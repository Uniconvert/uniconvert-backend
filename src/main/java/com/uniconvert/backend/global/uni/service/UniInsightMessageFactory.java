package com.uniconvert.backend.global.uni.service;

import com.uniconvert.backend.global.uni.dto.UniMessageResponse;
import com.uniconvert.backend.global.uni.enums.UniMessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class UniInsightMessageFactory {

    private final MessageSource messageSource;

    public List<UniMessageResponse> createExpenseInsights(
            BigDecimal todayExpense,
            String topCategory,
            BigDecimal remainingBudget,
            String currencySymbol
    ) {
        List<UniMessageResponse> insights = new ArrayList<>();
        String symbol = defaultSymbol(currencySymbol);

        if (todayExpense != null) {
            insights.add(
                    insight(
                            "EXPENSE_INSIGHT_TODAY",
                            symbol,
                            formatMoney(todayExpense)
                    )
            );
        }

        if (topCategory != null && !topCategory.isBlank()) {
            insights.add(
                    insight(
                            "EXPENSE_INSIGHT_TOP_CATEGORY",
                            topCategory
                    )
            );
        }

        if (remainingBudget != null) {
            insights.add(
                    insight(
                            "EXPENSE_INSIGHT_REMAINING_BUDGET",
                            symbol,
                            formatMoney(remainingBudget)
                    )
            );
        }

        return List.copyOf(insights);
    }

    public List<UniMessageResponse> createPotInsights(
            String potName,
            BigDecimal savedAmount,
            BigDecimal targetAmount,
            BigDecimal thisMonthAmount,
            String currencySymbol
    ) {
        List<UniMessageResponse> insights = new ArrayList<>();
        String symbol = defaultSymbol(currencySymbol);

        BigDecimal saved = defaultZero(savedAmount);
        BigDecimal target = defaultZero(targetAmount);

        if (target.signum() > 0) {
            BigDecimal progress = saved
                    .multiply(BigDecimal.valueOf(100))
                    .divide(
                            target,
                            1,
                            RoundingMode.HALF_UP
                    );

            insights.add(
                    insight(
                            "POTS_INSIGHT_PROGRESS",
                            potName,
                            progress.stripTrailingZeros()
                                    .toPlainString()
                    )
            );

            BigDecimal remaining =
                    target.subtract(saved).max(BigDecimal.ZERO);

            insights.add(
                    insight(
                            "POTS_INSIGHT_REMAINING",
                            symbol,
                            formatMoney(remaining)
                    )
            );
        }

        if (thisMonthAmount != null) {
            insights.add(
                    insight(
                            "POTS_INSIGHT_THIS_MONTH",
                            symbol,
                            formatMoney(thisMonthAmount)
                    )
            );
        }

        return List.copyOf(insights);
    }

    /**
     * changeRate는 5를 5%로 사용하는 기준입니다.
     */
    public List<UniMessageResponse> createReportInsights(
            BigDecimal changeRate,
            String weeklyMaxDate
    ) {
        List<UniMessageResponse> insights = new ArrayList<>();

        if (changeRate != null && changeRate.signum() > 0) {
            insights.add(
                    insight(
                            "REPORT_INSIGHT_INCREASE",
                            formatPercent(changeRate.abs())
                    )
            );
        } else if (changeRate != null
                && changeRate.signum() < 0) {

            insights.add(
                    insight(
                            "REPORT_INSIGHT_DECREASE",
                            formatPercent(changeRate.abs())
                    )
            );
        }

        if (weeklyMaxDate != null
                && !weeklyMaxDate.isBlank()) {

            insights.add(
                    insight(
                            "REPORT_INSIGHT_WEEKLY_MAX",
                            weeklyMaxDate
                    )
            );
        }

        return List.copyOf(insights);
    }

    public List<UniMessageResponse> createMemoInsights(
            Integer memoCount
    ) {
        if (memoCount == null || memoCount <= 0) {
            return List.of();
        }

        return List.of(
                insight(
                        "MEMO_INSIGHT_COUNT",
                        memoCount
                )
        );
    }

    public List<UniMessageResponse> createCalculatorInsights(
            BigDecimal rateChangePercent
    ) {
        if (rateChangePercent == null
                || rateChangePercent.signum() == 0) {
            return List.of();
        }

        String direction = getMessage(
                rateChangePercent.signum() > 0
                        ? "CALCULATOR_DIRECTION_UP"
                        : "CALCULATOR_DIRECTION_DOWN"
        );

        return List.of(
                insight(
                        "CALCULATOR_INSIGHT_RATE_CHANGE",
                        formatPercent(rateChangePercent.abs()),
                        direction
                )
        );
    }

    private UniMessageResponse insight(
            String key,
            Object... args
    ) {
        return new UniMessageResponse(
                key,
                getMessage(key, args),
                UniMessageType.INSIGHT
        );
    }

    private String getMessage(
            String key,
            Object... args
    ) {
        Locale locale = LocaleContextHolder.getLocale();

        String koreanFallback = messageSource.getMessage(
                key,
                args,
                key,
                Locale.KOREAN
        );

        return messageSource.getMessage(
                key,
                args,
                koreanFallback,
                locale
        );
    }

    private String formatMoney(BigDecimal amount) {
        NumberFormat formatter =
                NumberFormat.getNumberInstance(Locale.KOREA);

        return formatter.format(
                amount.setScale(
                        0,
                        RoundingMode.HALF_UP
                )
        );
    }

    private String formatPercent(BigDecimal percent) {
        return percent
                .setScale(1, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private BigDecimal defaultZero(BigDecimal amount) {
        return amount != null
                ? amount
                : BigDecimal.ZERO;
    }

    private String defaultSymbol(String symbol) {
        return symbol != null
                ? symbol
                : "";
    }
}
