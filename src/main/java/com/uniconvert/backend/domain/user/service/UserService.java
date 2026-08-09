package com.uniconvert.backend.domain.user.service;

import com.uniconvert.backend.domain.user.dto.request.UserUpdateRequest;
import com.uniconvert.backend.domain.user.dto.response.UserMeResponse;
import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }
}