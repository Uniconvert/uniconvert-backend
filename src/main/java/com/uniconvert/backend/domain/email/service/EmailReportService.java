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

    @Transactional(readOnly = true)
    public void sendReport(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);

        List<Expense> todayExpenses = expenseRepository.findAllInPeriod(
                userId, today.atStartOfDay(), today.atTime(23, 59, 59));

        BigDecimal todayTotal = todayExpenses.stream()
                .map(Expense::getConvertedAmountHome)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        YearMonth yearMonth = YearMonth.from(today);
        BigDecimal remainingBudget = expenseService.getRemainingBudget(userId, yearMonth);

        ReportSummaryResponse weekly = reportService.getSummary(userId, weekAgo, today);

        // 합계 보조 통화 — 사용자 등록 현지 통화로 환산 (없으면 보조 표시 생략)
        String localCurrency = user.getLocalCurrencyCode();
        BigDecimal todayTotalLocal = null;
        BigDecimal remainingBudgetLocal = null;
        if (localCurrency != null && !localCurrency.isBlank() && !localCurrency.equals(user.getHomeCurrencyCode())) {
            ConversionResult rate = exchangeRateService.getConversionRate(user.getHomeCurrencyCode(), localCurrency, today);
            todayTotalLocal = todayTotal.multiply(rate.rate()).setScale(2, RoundingMode.HALF_UP);
            remainingBudgetLocal = remainingBudget.multiply(rate.rate()).setScale(2, RoundingMode.HALF_UP);
        }

        String html = buildHtml(user, today, todayTotal, todayTotalLocal, remainingBudget, remainingBudgetLocal,
                todayExpenses, weekly.dailyAmounts(), localCurrency);
        String subject = today.format(DATE_FMT) + " 리포트 — 오늘 하루 지출을 정리했어요";

        emailService.sendHtmlEmail(user.getEmail(), subject, html);
    }

    private String buildHtml(User user, LocalDate today, BigDecimal todayTotal, BigDecimal todayTotalLocal,
                             BigDecimal remainingBudget, BigDecimal remainingBudgetLocal,
                             List<Expense> todayExpenses, List<DailyAmount> weeklyAmounts, String localCurrency) {

        String homeSymbol = currencySymbol(user.getHomeCurrencyCode());
        BigDecimal maxWeekly = weeklyAmounts.stream()
                .map(DailyAmount::amount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ONE);
        if (maxWeekly.compareTo(BigDecimal.ZERO) == 0) maxWeekly = BigDecimal.ONE;

        StringBuilder sb = new StringBuilder();

        // 바깥 wrapper (연한 하늘색 배경)
        sb.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='background:#eaf1fb;padding:40px 0;'>");
        sb.append("<tr><td align='center'>");

        // 흰 카드 (여기가 전체 콘텐츠를 담는 유일한 테이블)
        sb.append("<table role='presentation' width='520' cellpadding='0' cellspacing='0' style='background:#ffffff;border-radius:24px;border:1px solid #dce6f5;font-family:Apple SD Gothic Neo,Malgun Gothic,sans-serif;'>");
        sb.append("<tr><td style='padding:36px;'>");

        // 제목
        sb.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0'>");
        sb.append("<tr><td align='center' style='font-size:20px;font-weight:700;color:#1e2a3a;padding-bottom:4px;'>")
                .append(today.format(DATE_FMT)).append(" 리포트</td></tr>");
        sb.append("<tr><td align='center' style='font-size:14px;color:#a5b1c2;padding-bottom:24px;'>오늘 하루 지출을 정리했어요</td></tr>");
        sb.append("</table>");

        // 총 지출 / 남은 예산
        sb.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0'><tr>");
        sb.append("<td width='45%' align='center'>");
        sb.append("<div style='font-size:13px;color:#a5b1c2;margin-bottom:4px;'>총 지출 금액</div>");
        sb.append("<div style='font-size:22px;font-weight:700;color:#4a90e2;'>").append(homeSymbol).append(" ").append(format(todayTotal)).append("</div>");
        if (todayTotalLocal != null) {
            sb.append("<div style='font-size:12px;color:#a5b1c2;'>").append(localCurrency).append(" ").append(format(todayTotalLocal)).append("</div>");
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

        // 오늘 지출 내역
        sb.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0'>");
        sb.append("<tr><td style='font-size:15px;font-weight:700;color:#1e2a3a;padding-bottom:12px;'>오늘 지출 내역</td></tr>");
        if (todayExpenses.isEmpty()) {
            sb.append("<tr><td style='color:#a5b1c2;font-size:13px;'>오늘 등록된 지출이 없습니다.</td></tr>");
        }
        for (Expense e : todayExpenses) {
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

        // 이번 주 지출 흐름
        sb.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0'>");
        sb.append("<tr><td style='font-size:15px;font-weight:700;color:#1e2a3a;padding-bottom:16px;'>이번 주 지출 흐름</td></tr>");
        sb.append("<tr><td>");
        sb.append("<table role='presentation' width='100%' cellpadding='0' cellspacing='0'><tr>");

        LocalDate maxDay = weeklyAmounts.stream()
                .max((a, b) -> a.amount().compareTo(b.amount()))
                .map(DailyAmount::date)
                .orElse(null);

        for (DailyAmount d : weeklyAmounts) {
            boolean isToday = d.date().equals(today);
            boolean isMax = d.date().equals(maxDay);
            int heightPx = Math.max(10, d.amount().multiply(BigDecimal.valueOf(90))
                    .divide(maxWeekly, 0, RoundingMode.HALF_UP).intValue());
            String barColor = isMax ? "#3C688E" : (isToday ? "#6AADEA" : "#A8C1D6");
            sb.append("<td align='center' style='vertical-align:bottom;padding:0 6px;'>");
            if (isToday) {
                sb.append("<div style='font-size:11px;color:#3C688E;background:#eaf1fb;border-radius:10px;padding:3px 8px;margin-bottom:6px;white-space:nowrap;'>")
                        .append(homeSymbol).append(format(d.amount())).append("</div>");
            }
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

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
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