package com.uniconvert.backend.domain.auth.service;

import com.uniconvert.backend.domain.auth.dto.response.LoginResponse;
import com.uniconvert.backend.domain.auth.entity.AuthIdentity;
import com.uniconvert.backend.domain.auth.enums.AuthProvider;
import com.uniconvert.backend.domain.auth.repository.AuthIdentityRepository;
import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final AuthIdentityRepository authIdentityRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    @Transactional
    public LoginResponse googleLogin(String idToken) {
        GoogleTokenVerifier.GoogleUserInfo googleUserInfo =
                googleTokenVerifier.verify(idToken);

        User user = authIdentityRepository
                .findByProviderAndProviderUserId(
                        AuthProvider.GOOGLE,
                        googleUserInfo.getProviderUserId()
                )
                .map(AuthIdentity::getUser)
                .orElseGet(() -> createOrLinkGoogleUser(googleUserInfo));

        return tokenService.issueLoginResponse(user);
    }

    private User createOrLinkGoogleUser(
            GoogleTokenVerifier.GoogleUserInfo googleUserInfo
    ) {
        User user = userRepository.findByEmail(googleUserInfo.getEmail())
                .orElseGet(() -> userRepository.save(
                        new User(
                                googleUserInfo.getEmail(),
                                null,
                                resolveNickname(googleUserInfo)
                        )
                ));

        AuthIdentity authIdentity = new AuthIdentity(
                AuthProvider.GOOGLE,
                googleUserInfo.getProviderUserId(),
                googleUserInfo.getEmail(),
                user
        );

        authIdentityRepository.save(authIdentity);

        return user;
    }

    private String resolveNickname(
            GoogleTokenVerifier.GoogleUserInfo googleUserInfo
    ) {
        if (googleUserInfo.getName() != null
                && !googleUserInfo.getName().isBlank()) {
            return googleUserInfo.getName();
        }

        return googleUserInfo.getEmail().split("@")[0];
    }
}