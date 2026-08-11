package com.uniconvert.backend.domain.email.service;

import com.uniconvert.backend.domain.email.config.SesProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import com.uniconvert.backend.global.exception.CustomException;
import com.uniconvert.backend.global.exception.ErrorCode;
import software.amazon.awssdk.services.ses.model.MessageRejectedException;

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
        } catch (MessageRejectedException e) {
            // SES 샌드박스 상태에서 미인증 수신자에게 발송 시도한 경우 — 정상적으로 예상되는 상황이라 warn으로 기록
            log.warn("이메일 발송 거부(샌드박스/미인증 수신자): to={}, error={}", toAddress, e.awsErrorDetails().errorMessage());
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        } catch (SesException e) {
            log.error("이메일 발송 실패: to={}, error={}", toAddress, e.awsErrorDetails().errorMessage());
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }
}