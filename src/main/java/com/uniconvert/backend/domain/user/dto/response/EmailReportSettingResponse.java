package com.uniconvert.backend.domain.user.dto.response;

import com.uniconvert.backend.domain.user.entity.User;

import java.time.LocalTime;

public record EmailReportSettingResponse(
        boolean enabled,
        LocalTime sendTime,
        String frequency
) {
    public static EmailReportSettingResponse from(User user) {
        return new EmailReportSettingResponse(
                Boolean.TRUE.equals(user.getEmailReportEnabled()),
                user.getEmailReportSendTime(),
                user.getEmailReportFrequency()
        );
    }
}