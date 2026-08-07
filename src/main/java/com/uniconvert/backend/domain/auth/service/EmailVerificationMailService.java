package com.uniconvert.backend.domain.auth.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationMailService {

    private final SesClient sesClient;

    @Value("${aws.ses.enabled:false}")
    private boolean sesEnabled;

    @Value("${aws.ses.from-email:noreply@uniconvert.dev}")
    private String fromEmail;

    @Value("${app.email-verification-url:http://localhost:5173/email-verification}")
    private String emailVerificationUrl;

    public void sendVerificationMail(String email, String rawToken) {
        String verificationUrl = emailVerificationUrl
                + "?token="
                + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);

        if (!sesEnabled) {
            log.info("[DEV ONLY] Email verification mail disabled. to={}, url={}", email, verificationUrl);
            return;
        }

        SendEmailRequest request = SendEmailRequest.builder()
                .source(fromEmail)
                .destination(Destination.builder()
                        .toAddresses(email)
                        .build())
                .message(Message.builder()
                        .subject(Content.builder()
                                .charset("UTF-8")
                                .data("Uniconvert 이메일 인증")
                                .build())
                        .body(Body.builder()
                                .text(Content.builder()
                                        .charset("UTF-8")
                                        .data("아래 링크를 눌러 이메일 인증을 완료해 주세요.\n\n" + verificationUrl)
                                        .build())
                                .build())
                        .build())
                .build();

        sesClient.sendEmail(request);
    }
}
