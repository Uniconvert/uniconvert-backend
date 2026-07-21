package com.uniconvert.backend.domain.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(
            @Value("${oauth.google.client-id}") String googleClientId
    ) {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        )
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    public GoogleUserInfo verify(String idToken) {
        try {
            GoogleIdToken verifiedToken = verifier.verify(idToken);

            if (verifiedToken == null) {
                throw new IllegalArgumentException("유효하지 않은 Google ID Token입니다.");
            }

            GoogleIdToken.Payload payload = verifiedToken.getPayload();

            Boolean emailVerified = payload.getEmailVerified();
            if (emailVerified == null || !emailVerified) {
                throw new IllegalArgumentException("Google 이메일 인증이 완료되지 않은 계정입니다.");
            }

            return new GoogleUserInfo(
                    payload.getSubject(),
                    payload.getEmail(),
                    (String) payload.get("name"),
                    (String) payload.get("picture")
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Google ID Token 검증에 실패했습니다.", e);
        }
    }

    @Getter
    public static class GoogleUserInfo {

        private final String providerUserId;
        private final String email;
        private final String name;
        private final String picture;

        public GoogleUserInfo(
                String providerUserId,
                String email,
                String name,
                String picture
        ) {
            this.providerUserId = providerUserId;
            this.email = email;
            this.name = name;
            this.picture = picture;
        }
    }
}