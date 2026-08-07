package com.uniconvert.backend.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Configuration
public class LocaleConfig {

    public static final String LANGUAGE_HEADER = "X-Browser-Language";

    @Bean
    public LocaleResolver localeResolver() {
        return new BrowserLanguageLocaleResolver();
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource =
                new ResourceBundleMessageSource();

        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding(
                StandardCharsets.UTF_8.name()
        );
        messageSource.setFallbackToSystemLocale(false);

        return messageSource;
    }

    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        return validator;
    }

    private static class BrowserLanguageLocaleResolver
            implements LocaleResolver {

        @Override
        public Locale resolveLocale(
                HttpServletRequest request
        ) {
            String language =
                    request.getHeader(LANGUAGE_HEADER);

            if (language == null || language.isBlank()) {
                return Locale.KOREAN;
            }

            String normalized = language
                    .split(",", 2)[0]
                    .trim()
                    .replace('_', '-')
                    .toLowerCase(Locale.ROOT);

            if (normalized.startsWith("ko")) {
                return Locale.KOREAN;
            }

            if (normalized.startsWith("en")) {
                return Locale.ENGLISH;
            }

            if (normalized.startsWith("zh")) {
                return Locale.CHINESE;
            }

            if (normalized.startsWith("ja")) {
                return Locale.JAPANESE;
            }

            return Locale.KOREAN;
        }

        @Override
        public void setLocale(
                HttpServletRequest request,
                HttpServletResponse response,
                Locale locale
        ) {
            /*
             * 정책상 사용자가 언어를 수동 변경하지 않으므로
             * 서버에서 Locale을 별도로 변경하지 않습니다.
             */
        }
    }
}
