package com.uniconvert.backend.domain.email.controller;

import com.uniconvert.backend.domain.email.service.EmailReportService;
import com.uniconvert.backend.global.response.ApiResponse;
import com.uniconvert.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports/email")
@RequiredArgsConstructor
@Tag(name = "이메일 리포트", description = "일일 리포트 이메일 발송")
public class EmailController {

    private final EmailReportService emailReportService;

    @Operation(summary = "오늘의 리포트 이메일 발송", description = "'이메일로 리포트 보내기' 버튼. 본인 가입 이메일로 오늘자 리포트를 발송한다.")
    @PostMapping("/daily")
    public ApiResponse<Void> sendDailyReport(@AuthenticationPrincipal CustomUserDetails userDetails) {
        emailReportService.sendDailyReport(userDetails.getUserId());
        return ApiResponse.success(null);
    }
}