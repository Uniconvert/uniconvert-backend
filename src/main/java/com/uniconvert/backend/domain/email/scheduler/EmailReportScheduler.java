package com.uniconvert.backend.domain.email.scheduler;

import com.uniconvert.backend.domain.email.service.EmailReportService;
import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 매분 실행되며, emailReportEnabled=true인 사용자 중
 * 현재 시:분이 설정한 sendTime과 일치하는 사용자에게 리포트를 발송한다.
 *
 * - DAILY: 매일 발송
 * - WEEKLY: 매주 월요일에만 발송 (지난주 월~일 리포트)
 * - MONTHLY: 매월 1일에만 발송 (지난달 리포트)
 *
 * 프론트 화면에 요일/날짜 선택 UI가 없어 "매주=월요일 고정",
 * "매월=1일 고정"으로 설계했다 (다른 요일/날짜 지원은 추후 별도 컬럼 필요).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailReportScheduler {

    private final UserRepository userRepository;
    private final EmailReportService emailReportService;

    @Scheduled(cron = "0 * * * * *") // 매분 정각(0초)에 실행
    public void sendScheduledReports() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        LocalDate today = LocalDate.now();
        boolean isMonday = today.getDayOfWeek() == DayOfWeek.MONDAY;
        boolean isFirstDayOfMonth = today.getDayOfMonth() == 1;

        List<User> targets = userRepository.findAllByEmailReportEnabledTrue();

        for (User user : targets) {
            LocalTime sendTime = user.getEmailReportSendTime();
            String frequency = user.getEmailReportFrequency();

            if (sendTime == null || frequency == null) {
                continue;
            }

            if (!sendTime.withSecond(0).withNano(0).equals(now)) {
                continue;
            }

            try {
                switch (frequency) {
                    case "DAILY" -> emailReportService.sendDailyReport(user.getUserId());
                    case "WEEKLY" -> {
                        if (isMonday) {
                            emailReportService.sendWeeklyReport(user.getUserId());
                        }
                    }
                    case "MONTHLY" -> {
                        if (isFirstDayOfMonth) {
                            emailReportService.sendMonthlyReport(user.getUserId());
                        }
                    }
                    default -> log.warn("[EmailReportScheduler] 알 수 없는 frequency 값 - userId: {}, frequency: {}",
                            user.getUserId(), frequency);
                }
            } catch (Exception e) {
                // 한 사용자 발송 실패가 나머지 사용자 발송을 막으면 안 됨
                log.error("[EmailReportScheduler] 리포트 발송 실패 - userId: {}, error: {}",
                        user.getUserId(), e.getMessage());
            }
        }
    }
}