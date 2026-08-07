package com.uniconvert.backend.domain.auth.service;

import com.uniconvert.backend.domain.auth.entity.LocalCredential;
import com.uniconvert.backend.domain.auth.repository.LocalCredentialRepository;
import com.uniconvert.backend.global.exception.CustomException;
import com.uniconvert.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final LocalCredentialRepository localCredentialRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int recordFailure(Long localCredentialId) {
        LocalCredential credential = localCredentialRepository.findById(localCredentialId)
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_INVALID_CREDENTIALS));

        return credential.increaseFailedLoginCount();
    }
}
