package com.uniconvert.backend.global.uni.service;

import com.uniconvert.backend.global.uni.dto.UniMessageBundleResponse;
import com.uniconvert.backend.global.uni.dto.UniMessageResponse;
import com.uniconvert.backend.global.uni.enums.UniSection;
import com.uniconvert.backend.global.uni.service.UniMessageCatalog.UniMessageDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UniMessageService {

    private final MessageSource messageSource;

    public UniMessageBundleResponse createBundle(
            UniSection section,
            List<UniMessageResponse> insights
    ) {
        List<UniMessageResponse> entryMessages =
                UniMessageCatalog.getEntryMessages(section)
                        .stream()
                        .map(this::localize)
                        .toList();

        List<UniMessageResponse> randomMessages =
                new ArrayList<>();

        /*
         * 실제 데이터로 만든 인사이트를 우선 랜덤 풀에 포함합니다.
         */
        if (insights != null) {
            randomMessages.addAll(insights);
        }

        /*
         * 화면별 정적 랜덤 멘트를 현재 요청 언어로 변환해 추가합니다.
         */
        randomMessages.addAll(
                UniMessageCatalog.getRandomMessages(section)
                        .stream()
                        .map(this::localize)
                        .toList()
        );

        return new UniMessageBundleResponse(
                List.copyOf(entryMessages),
                List.copyOf(randomMessages)
        );
    }

    private UniMessageResponse localize(
            UniMessageDefinition definition
    ) {
        return new UniMessageResponse(
                definition.key(),
                getMessage(definition.key()),
                definition.type()
        );
    }

    private String getMessage(
            String key,
            Object... args
    ) {
        Locale locale = LocaleContextHolder.getLocale();

        String koreanFallback = messageSource.getMessage(
                key,
                args,
                key,
                Locale.KOREAN
        );

        return messageSource.getMessage(
                key,
                args,
                koreanFallback,
                locale
        );
    }
}
