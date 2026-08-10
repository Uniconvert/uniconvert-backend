package com.uniconvert.backend.domain.user.service;

import com.uniconvert.backend.domain.user.dto.request.UserUpdateRequest;
import com.uniconvert.backend.domain.user.dto.response.UserMeResponse;
import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uniconvert.backend.domain.user.dto.request.EmailReportSettingRequest;
import com.uniconvert.backend.domain.user.dto.response.EmailReportSettingResponse;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserMeResponse getMyInfo(Long userId) {
        User user = findUser(userId);
        return UserMeResponse.from(user);
    }

    @Transactional
    public UserMeResponse updateMyInfo(Long userId, UserUpdateRequest request) {
        User user = findUser(userId);

        if (request.nickname() != null) {
            user.updateNickname(request.nickname().trim());
        }

        if (request.profileImageKey() != null) {
            user.updateProfileImageKey(request.profileImageKey());
        }

        if (request.primaryGoal() != null) {
            user.updatePrimaryGoal(request.primaryGoal());
        }

        return UserMeResponse.from(user);
    }

    @Transactional(readOnly = true)
    public EmailReportSettingResponse getEmailReportSetting(Long userId) {
        User user = findUser(userId);
        return EmailReportSettingResponse.from(user);
    }

    @Transactional
    public EmailReportSettingResponse updateEmailReportSetting(Long userId, EmailReportSettingRequest request) {
        User user = findUser(userId);

        if (Boolean.TRUE.equals(request.enabled())) {
            if (request.sendTime() == null || request.frequency() == null) {
                throw new IllegalArgumentException("이메일 리포트를 켜려면 받는 시간과 발송 주기를 함께 입력해야 합니다.");
            }
        }

        user.updateEmailReportSetting(
                request.enabled(),
                request.sendTime(),
                request.frequency() != null ? request.frequency().name() : null
        );

        return EmailReportSettingResponse.from(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }
}