package com.uniconvert.backend.domain.user.dto.request;

import com.uniconvert.backend.domain.user.enums.EmailReportFrequency;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record EmailReportSettingRequest(

        @NotNull(message = "{validation.email_report.enabled_required}")
        Boolean enabled,

        // enabled=true일 때만 필수 — 서비스 레이어에서 검증
        LocalTime sendTime,

        // enabled=true일 때만 필수 — 서비스 레이어에서 검증
        EmailReportFrequency frequency
) {
}