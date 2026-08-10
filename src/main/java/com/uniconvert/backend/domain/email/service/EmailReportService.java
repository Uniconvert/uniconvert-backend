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
        sendPeriodReport(userId, today, today, "오늘 하루 지출을 정리했어요", true);
    }

    // 스케줄러 WEEKLY용 — 지난주 월~일, 항목 나열 없이 총액+그래프만
    @Transactional(readOnly = true)
    public void sendWeeklyReport(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate lastMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
        LocalDate lastSunday = lastMonday.plusDays(6);
        sendPeriodReport(userId, lastMonday, lastSunday, "지난 한 주 지출을 정리했어요", false);
    }

    // 스케줄러 MONTHLY용 — 지난달 1일~말일, 항목 나열 없이 총액+그래프만
    @Transactional(readOnly = true)
    public void sendMonthlyReport(Long userId) {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        sendPeriodReport(userId, lastMonth.atDay(1), lastMonth.atEndOfMonth(), "지난달 지출을 정리했어요", false);
    }

    private void sendPeriodReport(
            Long userId,
            LocalDate periodStart,
            LocalDate periodEnd,
            String subtitle,
            boolean includeItemList
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        List<Expense> periodExpenses = expenseRepository.findAllInPeriod(
                userId, periodStart.atStartOfDay(), periodEnd.atTime(23, 59, 59));

        ReportSummaryResponse summary = reportService.getSummary(userId, periodStart, periodEnd);
        BigDecimal periodTotal = summary.totalAmount();

        YearMonth currentMonth = YearMonth.now();
        BigDecimal remainingBudget = expenseService.getRemainingBudget(userId, currentMonth);

        // 합계 보조 통화 — 사용자 등록 현지 통화로 환산 (없으면 보조 표시 생략)
        String localCurrency = user.getLocalCurrencyCode();
        BigDecimal periodTotalLocal = null;
        BigDecimal remainingBudgetLocal = null;
        if (localCurrency != null && !localCurrency.isBlank() && !localCurrency.equals(user.getHomeCurrencyCode())) {
            ConversionResult rate = exchangeRateService.getConversionRate(
                    user.getHomeCurrencyCode(), localCurrency, periodEnd);
            periodTotalLocal = periodTotal.multiply(rate.rate()).setScale(2, RoundingMode.HALF_UP);
            remainingBudgetLocal = remainingBudget.multiply(rate.rate()).setScale(2, RoundingMode.HALF_UP);
        }

        String html = buildHtml(user, periodEnd, subtitle, periodTotal, periodTotalLocal,
                remainingBudget, remainingBudgetLocal, periodExpenses, summary.dailyAmounts(),
                localCurrency, includeItemList);
        String subject = periodEnd.format(DATE_FMT) + " 리포트 — " + subtitle;

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

        // 바깥 wrapper (연한 하늘색 배경)
        sb.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='background:#eaf1fb;padding:40px 0;'>");
        sb.append("<tr><td align='center'>");

        // 흰 카드
        sb.append("<table role='presentation' width='520' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:24px;border:1px solid #dce6f5;font-family:Apple SD Gothic Neo,Malgun Gothic,sans-serif;'>");
        sb.append("<tr><td style='padding:36px;'>");

        // 제목
        sb.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0'>");
        sb.append("<tr><td align='center' style='font-size:20px;font-weight:700;color:#1e2a3a;padding-bottom:4px;'>")
                .append(referenceDate.format(DATE_FMT)).append(" 리포트</td></tr>");
        sb.append("<tr><td align='center' style='font-size:14px;color:#a5b1c2;padding-bottom:24px;'>").append(subtitle).append("</td></tr>");
        sb.append("</table>");

        // 총 지출 / 남은 예산
        sb.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0'><tr>");
        sb.append("<td width='45%' align='center'>");
        sb.append("<div style='font-size:13px;color:#a5b1c2;margin-bottom:4px;'>총 지출 금액</div>");
        sb.append("<div style='font-size:22px;font-weight:700;color:#4a90e2;'>").append(homeSymbol).append(" ").append(format(periodTotal)).append("</div>");
        if (periodTotalLocal != null) {
            sb.append("<div style='font-size:12px;color:#a5b1c2;'>").append(localCurrency).append(" ").append(format(periodTotalLocal)).append("</div>");
        }
        sb.append("</td>");
        sb.append("<td width='10%' align='center' style='font-size:20px;color:#4a90e2;'>&raquo;</td>");
        sb.append("<td width='45%' align='center'>");
        sb.append("<div style='font-size:13px;color:#a5b1c2;margin-bottom:4px;'>남은 예산</div>");
        sb.append("<div style='font-size:22px;font-weight:700;color:#1e2a3a;'>").append(homeSymbol).append(" ").append(format(remainingBudget)).append("</div>");
        if (remainingBudgetLocal != null) {
            sb.append("<div style='font-size:12px;color:#a5b1c2;'>").append(localCurrency).append(" ").append(format(remainingBudgetLocal)).append("</div>");
        }
        sb.append("</td>");
        sb.append("</tr></table>");

        sb.append("<div style='border-top:1px solid #eee;margin:20px 0;'></div>");

        // 개별 지출 항목 (일간 리포트에서만 포함)
        if (includeItemList) {
            sb.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0'>");
            sb.append("<tr><td style='font-size:15px;font-weight:700;color:#1e2a3a;padding-bottom:12px;'>오늘 지출 내역</td></tr>");
            if (periodExpenses.isEmpty()) {
                sb.append("<tr><td style='color:#a5b1c2;font-size:13px;'>오늘 등록된 지출이 없습니다.</td></tr>");
            }
            for (Expense e : periodExpenses) {
                sb.append("<tr><td style='padding:8px 0;'>");
                sb.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0'><tr>");
                sb.append("<td width='40' style='padding-right:12px;'>");
                sb.append("<div style='width:40px;height:40px;background:#f5f6fa;border-radius:10px;text-align:center;line-height:40px;font-size:18px;'>").append(categoryEmoji(e.getCategoryId())).append("</div>");
                sb.append("</td>");
                sb.append("<td>");
                sb.append("<div style='font-size:14px;font-weight:600;color:#1e2a3a;'>").append(displayName(e)).append("</div>");
                sb.append("<div style='font-size:12px;color:#a5b1c2;'>").append(e.getSpentAt().toLocalTime().format(TIME_FMT)).append("</div>");
                sb.append("</td>");
                sb.append("<td align='right'>");
                sb.append("<div style='font-size:14px;font-weight:700;color:#1e2a3a;'>").append(homeSymbol).append(" ").append(format(e.getConvertedAmountHome())).append("</div>");
                sb.append("<div style='font-size:12px;color:#a5b1c2;'>").append(e.getOriginalCurrency()).append(" ").append(format(e.getOriginalAmount())).append("</div>");
                sb.append("</td>");
                sb.append("</tr></table>");
                sb.append("</td></tr>");
            }
            sb.append("</table>");
            sb.append("<div style='border-top:1px solid #eee;margin:20px 0;'></div>");
        }

        // 기간별 지출 흐름 (막대그래프) — 일간은 최근 7일 컨텍스트, 주간/월간은 조회 기간 그대로
        sb.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0'>");
        sb.append("<tr><td style='font-size:15px;font-weight:700;color:#1e2a3a;padding-bottom:16px;'>지출 흐름</td></tr>");
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
            String barColor = isMax ? "#3C688E" : (isReference ? "#6AADEA" : "#A8C1D6");
            sb.append("<td align='center' style='vertical-align:bottom;padding:0 6px;'>");
            sb.append("<div style='width:32px;height:").append(heightPx).append("px;background:").append(barColor).append(";border-radius:6px;margin:0 auto;'></div>");
            sb.append("<div style='font-size:12px;color:#ABABAB;margin-top:6px;'>").append(d.date().getDayOfMonth()).append("d</div>");
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