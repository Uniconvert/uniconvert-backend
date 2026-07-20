package com.uniconvert.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String API_BASE_PACKAGE = "com.uniconvert.backend";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    /**
     * server.servlet.context-path 대신 우리 컨트롤러에만 /api/v1 접두사를 붙인다.
     * springdoc이 등록하는 컨트롤러(org.springdoc.*)는 이 패키지 밖에 있어 영향을 받지 않으므로
     * Swagger UI(/swagger)와 OpenAPI 문서(/v3/api-docs)는 도메인 루트에서 그대로 노출된다.
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api/v1", HandlerTypePredicate.forBasePackage(API_BASE_PACKAGE));
    }
}
