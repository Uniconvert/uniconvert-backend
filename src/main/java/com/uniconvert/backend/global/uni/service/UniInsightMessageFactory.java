package com.uniconvert.backend.global.uni.service;

import com.uniconvert.backend.global.uni.dto.UniMessageResponse;
import com.uniconvert.backend.global.uni.enums.UniMessageType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class UniInsightMessageFactory {

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
                            "오늘 "
                                    + symbol
                                    + formatMoney(todayExpense)
                                    + " 사용했어요."
                    )
            );
        }

        if (topCategory != null && !topCategory.isBlank()) {
            insights.add(
                    insight(
                            "EXPENSE_INSIGHT_TOP_CATEGORY",
                            "이번 달 가장 많이 지출한 항목은 "
                                    + topCategory
                                    + "예요."
                    )
            );
        }

        if (remainingBudget != null) {
            insights.add(
                    insight(
                            "EXPENSE_INSIGHT_REMAINING_BUDGET",
                            "이번 달 사용 가능 금액은 "
                                    + symbol
                                    + formatMoney(remainingBudget)
                                    + " 남아 있어요."
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
                            potName
                                    + " Pot은 목표의 "
                                    + progress.stripTrailingZeros()
                                    .toPlainString()
                                    + "%까지 모였어요."
                    )
            );

            BigDecimal remaining =
                    target.subtract(saved).max(BigDecimal.ZERO);

            insights.add(
                    insight(
                            "POTS_INSIGHT_REMAINING",
                            "목표까지 "
                                    + symbol
                                    + formatMoney(remaining)
                                    + " 남아 있어요."
                    )
            );
        }

        if (thisMonthAmount != null) {
            insights.add(
                    insight(
                            "POTS_INSIGHT_THIS_MONTH",
                            "이번 달에는 "
                                    + symbol
                                    + formatMoney(thisMonthAmount)
                                    + " 배정했어요."
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
                            "오늘 지출이 어제보다 "
                                    + formatPercent(changeRate.abs())
                                    + "% 늘었어요."
                    )
            );
        } else if (changeRate != null
                && changeRate.signum() < 0) {

            insights.add(
                    insight(
                            "REPORT_INSIGHT_DECREASE",
                            "오늘 지출이 어제보다 "
                                    + formatPercent(changeRate.abs())
                                    + "% 줄었어요."
                    )
            );
        }

        if (weeklyMaxDate != null
                && !weeklyMaxDate.isBlank()) {

            insights.add(
                    insight(
                            "REPORT_INSIGHT_WEEKLY_MAX",
                            "이번 주에는 "
                                    + weeklyMaxDate
                                    + "에 가장 많이 지출했어요."
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
                        "지금까지 남겨둔 지출 메모가 "
                                + memoCount
                                + "개 있어요."
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

        String direction =
                rateChangePercent.signum() > 0
                        ? "올랐어요"
                        : "내렸어요";

        return List.of(
                insight(
                        "CALCULATOR_INSIGHT_RATE_CHANGE",
                        "오늘 환율이 전일보다 "
                                + formatPercent(
                                rateChangePercent.abs()
                        )
                                + "% "
                                + direction
                                + ". 계산할 때 참고해주세요."
                )
        );
    }

    private UniMessageResponse insight(
            String key,
            String message
    ) {
        return new UniMessageResponse(
                key,
                message,
                UniMessageType.INSIGHT
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
