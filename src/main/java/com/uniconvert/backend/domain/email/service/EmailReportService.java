package com.uniconvert.backend.domain.email.service;

import com.uniconvert.backend.domain.exchange.dto.response.ConversionResult;
import com.uniconvert.backend.domain.exchange.service.ExchangeRateService;
import com.uniconvert.backend.domain.expense.entity.Expense;
import com.uniconvert.backend.domain.expense.repository.ExpenseRepository;
import com.uniconvert.backend.domain.expense.service.ExpenseService;
import com.uniconvert.backend.domain.report.dto.response.DailyAmount;
import com.uniconvert.backend.domain.report.dto.response.ReportSummaryResponse;
import com.uniconvert.backend.domain.report.service.ReportService;
import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.domain.user.repository.UserRepository;
import com.uniconvert.backend.global.exception.CustomException;
import com.uniconvert.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailReportService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseService expenseService;
    private final ReportService reportService;
    private final UserRepository userRepository;
    private final ExchangeRateService exchangeRateService;
    private final EmailService emailService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M월 d일");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // 즉시발송 버튼(POST /reports/email/send)용 — 기존 동작 그대로 유지
    @Transactional(readOnly = true)
    public void sendReport(Long userId) {
        sendDailyReport(userId);
    }

    // 스케줄러 DAILY용 — 오늘 하루, 개별 지출 항목 포함
    @Transactional(readOnly = true)
    public void sendDailyReport(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate chartStart = today.minusDays(6);
        sendPeriodReport(userId, today, today, chartStart, today, "오늘 하루 지출을 정리했어요", true);
    }

    // 스케줄러 WEEKLY용 — 지난주 월~일, 항목 나열 없이 총액+그래프만
    @Transactional(readOnly = true)
    public void sendWeeklyReport(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate lastMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
        LocalDate lastSunday = lastMonday.plusDays(6);
        sendPeriodReport(userId, lastMonday, lastSunday, lastMonday, lastSunday, "지난 한 주 지출을 정리했어요", false);
    }

    @Transactional(readOnly = true)
    public void sendMonthlyReport(Long userId) {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        LocalDate start = lastMonth.atDay(1);
        LocalDate end = lastMonth.atEndOfMonth();
        sendPeriodReport(userId, start, end, start, end, "지난달 지출을 정리했어요", false);
    }

    private void sendPeriodReport(
            Long userId,
            LocalDate totalStart,
            LocalDate totalEnd,
            LocalDate chartStart,
            LocalDate chartEnd,
            String subtitle,
            boolean includeItemList
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        List<Expense> periodExpenses = expenseRepository.findAllInPeriod(
                userId, totalStart.atStartOfDay(), totalEnd.atTime(23, 59, 59));

        BigDecimal periodTotal = expenseRepository.sumConvertedAmountByPeriod(
                userId, totalStart.atStartOfDay(), totalEnd.atTime(23, 59, 59));
        if (periodTotal == null) {
            periodTotal = BigDecimal.ZERO;
        }

        // 막대그래프는 총액과 별개 범위로 조회 — 일간 리포트는 최근 7일 컨텍스트를 보여주기 위함
        ReportSummaryResponse chartSummary = reportService.getSummary(userId, chartStart, chartEnd);

        YearMonth currentMonth = YearMonth.now();
        BigDecimal remainingBudget = expenseService.getRemainingBudget(userId, currentMonth);

        String localCurrency = user.getLocalCurrencyCode();
        BigDecimal periodTotalLocal = null;
        BigDecimal remainingBudgetLocal = null;
        if (localCurrency != null && !localCurrency.isBlank() && !localCurrency.equals(user.getHomeCurrencyCode())) {
            ConversionResult rate = exchangeRateService.getConversionRate(
                    user.getHomeCurrencyCode(), localCurrency, totalEnd);
            periodTotalLocal = periodTotal.multiply(rate.rate()).setScale(2, RoundingMode.HALF_UP);
            remainingBudgetLocal = remainingBudget.multiply(rate.rate()).setScale(2, RoundingMode.HALF_UP);
        }

        String html = buildHtml(user, totalEnd, subtitle, periodTotal, periodTotalLocal,
                remainingBudget, remainingBudgetLocal, periodExpenses, chartSummary.dailyAmounts(),
                localCurrency, includeItemList);
        String subject = totalEnd.format(DATE_FMT) + " 리포트 — " + subtitle;

        emailService.sendHtmlEmail(user.getEmail(), subject, html);
    }

    private String buildHtml(User user, LocalDate referenceDate, String subtitle,
                             BigDecimal periodTotal, BigDecimal periodTotalLocal,
                             BigDecimal remainingBudget, BigDecimal remainingBudgetLocal,
                             List<Expense> periodExpenses, List<DailyAmount> chartAmounts,
                             String localCurrency, boolean includeItemList) {

        String homeSymbol = currencySymbol(user.getHomeCurrencyCode());
        BigDecimal maxAmount = chartAmounts.stream()
                .map(DailyAmount::amount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ONE);
        if (maxAmount.compareTo(BigDecimal.ZERO) == 0) maxAmount = BigDecimal.ONE;

        StringBuilder sb = new StringBuilder();

        // 바깥 wrapper — 프론트는 그라데이션(#6AADEA→#fff)이지만 이메일 클라이언트 호환을 위해 연한 단색 사용
        sb.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='background:#eaf1fb;padding:40px 0;'>");
        sb.append("<tr><td align='center'>");

        // 흰 카드 (emailModalInner에 대응, border-radius: 1.5rem = 24px)
        sb.append("<table role='presentation' width='520' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:24px;border:1px solid #dce6f5;font-family:Apple SD Gothic Neo,Malgun Gothic,sans-serif;'>");
        sb.append("<tr><td style='padding:36px;'>");

        // 헤더 (emailHeader)
        sb.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0'>");
        sb.append("<tr><td align='center' style='font-size:20px;font-weight:600;color:#3C688E;padding-bottom:8px;'>")
                .append(referenceDate.format(DATE_FMT)).append(" 리포트</td></tr>");
        sb.append("<tr><td align='center' style='font-size:16px;font-weight:500;color:#A8C1D6;padding-bottom:24px;'>").append(subtitle).append("</td></tr>");
        sb.append("</table>");

        // 총 지출 / 남은 예산 (emailSummary → summaryBox)
        sb.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0'><tr>");
        sb.append("<td width='45%' align='center'>");
        sb.append("<div style='font-size:15px;font-weight:500;color:#A3BDD6;margin-bottom:6px;'>총 지출 금액</div>");
        sb.append("<div style='font-size:22px;font-weight:600;color:#6AADEA;'>").append(homeSymbol).append(" ").append(format(periodTotal)).append("</div>");
        if (periodTotalLocal != null) {
            sb.append("<div style='font-size:13px;color:#A3BDD6;margin-top:2px;'>(").append(localCurrency).append(" ").append(format(periodTotalLocal)).append(")</div>");
        }
        sb.append("</td>");
        sb.append("<td width='10%' align='center' style='font-size:18px;color:#90b6d9;'>&raquo;</td>");
        sb.append("<td width='45%' align='center'>");
        sb.append("<div style='font-size:15px;font-weight:500;color:#A3BDD6;margin-bottom:6px;'>남은 예산</div>");
        sb.append("<div style='font-size:22px;font-weight:600;color:#3C688E;'>").append(homeSymbol).append(" ").append(format(remainingBudget)).append("</div>");
        if (remainingBudgetLocal != null) {
            sb.append("<div style='font-size:13px;color:#A3BDD6;margin-top:2px;'>(").append(localCurrency).append(" ").append(format(remainingBudgetLocal)).append(")</div>");
        }
        sb.append("</td>");
        sb.append("</tr></table>");

        sb.append("<div style='border-top:1px solid #F0F0F0;margin:24px 0;'></div>");

        // 지출 내역 (emailListSection → emailTxList)
        if (includeItemList) {
            sb.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0'>");
            sb.append("<tr><td style='font-size:17px;font-weight:500;color:#000000;padding-bottom:20px;'>오늘 지출 내역</td></tr>");
            if (periodExpenses.isEmpty()) {
                sb.append("<tr><td style='color:#999999;font-size:14px;text-align:center;padding:16px 0;'>오늘 기록된 지출이 없어요.</td></tr>");
            }
            for (Expense e : periodExpenses) {
                sb.append("<tr><td style='padding-bottom:20px;'>");
                sb.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0'><tr>");
                sb.append("<td width='40' style='padding-right:16px;'>");
                sb.append("<div style='width:40px;height:40px;background:#ffffff;border:1px solid #F0F0F0;border-radius:8px;text-align:center;line-height:40px;font-size:18px;'>").append(categoryEmoji(e.getCategoryId())).append("</div>");
                sb.append("</td>");
                sb.append("<td>");
                sb.append("<div style='font-size:15px;font-weight:500;color:#222222;'>").append(displayName(e)).append("</div>");
                sb.append("<div style='font-size:12px;color:#999999;margin-top:3px;'>").append(categoryName(e.getCategoryId())).append(" &bull; ").append(e.getSpentAt().toLocalTime().format(TIME_FMT)).append("</div>");
                sb.append("</td>");
                sb.append("<td align='right'>");
                sb.append("<div style='font-size:15px;font-weight:500;color:#222222;'>").append(homeSymbol).append(" ").append(format(e.getConvertedAmountHome())).append("</div>");
                sb.append("<div style='font-size:12px;color:#999999;margin-top:3px;'>").append(e.getOriginalCurrency()).append(" ").append(format(e.getOriginalAmount())).append("</div>");
                sb.append("</td>");
                sb.append("</tr></table>");
                sb.append("</td></tr>");
            }
            sb.append("</table>");
            sb.append("<div style='border-top:1px solid #F0F0F0;margin:24px 0;'></div>");
        }

        // 지출 흐름 (emailChartSection → emailChart)
        sb.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0'>");
        sb.append("<tr><td style='font-size:17px;font-weight:500;color:#000000;padding-bottom:20px;'>지출 흐름</td></tr>");
        sb.append("<tr><td>");
        sb.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0'><tr>");

        LocalDate maxDay = chartAmounts.stream()
                .max((a, b) -> a.amount().compareTo(b.amount()))
                .map(DailyAmount::date)
                .orElse(null);

        for (DailyAmount d : chartAmounts) {
            boolean isReference = d.date().equals(referenceDate);
            boolean isMax = d.date().equals(maxDay);
            int heightPx = Math.max(10, d.amount().multiply(BigDecimal.valueOf(90))
                    .divide(maxAmount, 0, RoundingMode.HALF_UP).intValue());
            // barMax(#3C688E) > barToday(#6AADEA) > default(#A8C1D6)
            String barColor = isMax ? "#3C688E" : (isReference ? "#6AADEA" : "#A8C1D6");
            sb.append("<td align='center' style='vertical-align:bottom;padding:0 6px;'>");
            if (isReference) {
                sb.append("<div style='font-size:11px;color:#6AADEA;background:#eff7ff;border:0.5px solid #6AADEA;border-radius:4px;padding:3px 8px;margin-bottom:6px;white-space:nowrap;'>")
                        .append(homeSymbol).append(" ").append(format(d.amount())).append("</div>");
            }
            sb.append("<div style='width:32px;height:").append(heightPx).append("px;background:").append(barColor).append(";border-radius:6px 6px 0 0;margin:0 auto;'></div>");
            sb.append("<div style='font-size:12px;color:#ABABAB;margin-top:8px;'>").append(d.date().getDayOfMonth()).append("d</div>");
            sb.append("</td>");
        }
        sb.append("</tr></table>");
        sb.append("</td></tr>");
        sb.append("</table>");

        // 카드 닫기
        sb.append("</td></tr>");
        sb.append("</table>");

        // 바깥 wrapper 닫기
        sb.append("</td></tr>");
        sb.append("</table>");

        return sb.toString();
    }

    private String currencySymbol(String currencyCode) {
        if (currencyCode == null) return "";
        return switch (currencyCode) {
            case "KRW" -> "₩";
            case "USD" -> "$";
            case "EUR" -> "€";
            case "JPY" -> "¥";
            case "CNY" -> "¥";
            default -> currencyCode + " ";
        };
    }

    private String categoryEmoji(Long categoryId) {
        if (categoryId == null) return "💰";
        return switch (categoryId.intValue()) {
            case 1 -> "🍔"; // 식비
            case 2 -> "🚌"; // 교통
            case 3 -> "🛍️"; // 쇼핑
            case 4 -> "📞"; // 통신
            case 5 -> "🎓"; // 학업
            case 6 -> "✈️"; // 여행
            case 7 -> "🏠"; // 주거
            case 8 -> "💰"; // 저축
            default -> "🔖"; // 기타
        };
    }

    private String format(BigDecimal value) {
        return value == null ? "0" : String.format("%,.0f", value);
    }

    private String displayName(Expense e) {
        String merchant = e.getMerchantName();
        if (merchant == null || merchant.isBlank() || merchant.equalsIgnoreCase("null")) {
            return categoryName(e.getCategoryId());
        }
        return merchant;
    }

    private String categoryName(Long categoryId) {
        if (categoryId == null) return "기타";
        return switch (categoryId.intValue()) {
            case 1 -> "식비";
            case 2 -> "교통";
            case 3 -> "쇼핑";
            case 4 -> "통신";
            case 5 -> "학업";
            case 6 -> "여행";
            case 7 -> "주거";
            case 8 -> "저축";
            default -> "기타";
        };
    }
}