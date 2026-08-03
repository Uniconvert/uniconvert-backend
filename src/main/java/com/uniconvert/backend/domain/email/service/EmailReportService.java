package com.uniconvert.backend.domain.email.service;

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
    private final EmailService emailService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M월 d일");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // "이메일로 리포트 보내기" 버튼 — 오늘자 일일 리포트를 본인 이메일로 발송
    @Transactional(readOnly = true)
    public void sendDailyReport(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);

        // 오늘 지출 목록
        List<Expense> todayExpenses = expenseRepository.findAllInPeriod(
                userId, today.atStartOfDay(), today.atTime(23, 59, 59));

        BigDecimal todayTotal = todayExpenses.stream()
                .map(Expense::getConvertedAmountHome)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 남은 예산 (기존 로직 재사용)
        YearMonth yearMonth = YearMonth.from(today);
        BigDecimal remainingBudget = expenseService.getRemainingBudget(userId, yearMonth);

        // 이번 주 지출 흐름 (기존 리포트 로직 재사용)
        ReportSummaryResponse weekly = reportService.getSummary(userId, weekAgo, today);

        String html = buildHtml(today, todayTotal, remainingBudget, todayExpenses, weekly.dailyAmounts());
        String subject = today.format(DATE_FMT) + " 리포트 — 오늘 하루 지출을 정리했어요";

        emailService.sendHtmlEmail(user.getEmail(), subject, html);
    }

    private String buildHtml(LocalDate today, BigDecimal todayTotal, BigDecimal remainingBudget,
                             List<Expense> todayExpenses, List<DailyAmount> weeklyAmounts) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family:sans-serif;max-width:480px;margin:0 auto;padding:24px;border:1px solid #eee;border-radius:16px;'>");
        sb.append("<h2 style='text-align:center;color:#2f3542;'>").append(today.format(DATE_FMT)).append(" 리포트</h2>");
        sb.append("<p style='text-align:center;color:#a5b1c2;'>오늘 하루 지출을 정리했어요</p>");

        sb.append("<div style='display:flex;justify-content:space-around;margin:20px 0;'>");
        sb.append("<div style='text-align:center;'><div style='color:#a5b1c2;'>총 지출 금액</div>")
                .append("<div style='font-size:20px;font-weight:bold;color:#3867d6;'>₩ ").append(format(todayTotal)).append("</div></div>");
        sb.append("<div style='text-align:center;'><div style='color:#a5b1c2;'>남은 예산</div>")
                .append("<div style='font-size:20px;font-weight:bold;'>₩ ").append(format(remainingBudget)).append("</div></div>");
        sb.append("</div><hr style='border:none;border-top:1px solid #eee;'/>");

        sb.append("<h3>오늘 지출 내역</h3>");
        for (Expense e : todayExpenses) {
            sb.append("<div style='display:flex;justify-content:space-between;padding:8px 0;'>");
            sb.append("<div>").append(nullToDash(e.getMerchantName())).append(" <span style='color:#a5b1c2;font-size:12px;'>")
                    .append(e.getSpentAt().toLocalTime().format(TIME_FMT)).append("</span></div>");
            sb.append("<div>₩ ").append(format(e.getConvertedAmountHome())).append("</div>");
            sb.append("</div>");
        }
        if (todayExpenses.isEmpty()) {
            sb.append("<p style='color:#a5b1c2;'>오늘 등록된 지출이 없습니다.</p>");
        }

        sb.append("<hr style='border:none;border-top:1px solid #eee;'/>");
        sb.append("<h3>이번 주 지출 흐름</h3>");
        sb.append("<table style='width:100%;'><tr>");
        for (DailyAmount d : weeklyAmounts) {
            sb.append("<td style='text-align:center;font-size:12px;color:#a5b1c2;'>")
                    .append(d.date().getDayOfMonth()).append("일<br/>₩").append(format(d.amount())).append("</td>");
        }
        sb.append("</tr></table>");

        sb.append("</div>");
        return sb.toString();
    }

    private String format(BigDecimal value) {
        return value == null ? "0" : String.format("%,.0f", value);
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}