package com.uniconvert.backend.domain.email.service;

import com.uniconvert.backend.domain.email.config.SesProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final SesClient sesClient;
    private final SesProperties sesProperties;

    public void sendHtmlEmail(String toAddress, String subject, String htmlBody) {
        Destination destination = Destination.builder()
                .toAddresses(toAddress)
                .build();

        Content subjectContent = Content.builder().data(subject).charset("UTF-8").build();
        Content bodyContent = Content.builder().data(htmlBody).charset("UTF-8").build();
        Body body = Body.builder().html(bodyContent).build();

        Message message = Message.builder()
                .subject(subjectContent)
                .body(body)
                .build();

        SendEmailRequest request = SendEmailRequest.builder()
                .source(sesProperties.getFromAddress())
                .destination(destination)
                .message(message)
                .build();

        try {
            sesClient.sendEmail(request);
            log.info("이메일 발송 성공: to={}", toAddress);
        } catch (SesException e) {
            log.error("이메일 발송 실패: to={}, error={}", toAddress, e.awsErrorDetails().errorMessage());
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }
}