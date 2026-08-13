package com.uniconvert.backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class SwaggerConfig {

    private static final String BROWSER_LANGUAGE_HEADER = "BrowserLanguageHeader";

    @Bean
    public OpenAPI openAPI() {
        String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Uniconvert API")
                        .version("v1")
                        .description("Uniconvert backend API documentation")
                        .contact(new Contact().name("Uniconvert")))
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(securitySchemeName)
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                                .addParameters(
                                        BROWSER_LANGUAGE_HEADER,
                                        new Parameter()
                                                .name("X-Browser-Language")
                                                .in("header")
                                                .required(false)
                                                .description("""
                                                        사용자의 브라우저 언어 정보입니다.

                                                        프론트엔드는 브라우저의 navigator.language 값을 전달합니다.
                                                        값이 없거나 지원하지 않는 값인 경우 서버는 ko-KR을 기본값으로 사용합니다.
                                                        """)
                                                .schema(
                                                        new Schema<String>()
                                                                .type("string")
                                                                .example("ko-KR")
                                                                ._default("ko-KR")
                                                )
                                )
                );
    }

    /**
     * 모든 실제 구현 API 경로에 X-Browser-Language 공통 헤더를 자동으로 노출한다.
     */
    @Bean
    @Order(1)
    public GlobalOpenApiCustomizer browserLanguageHeaderCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation ->
                            operation.addParametersItem(
                                    new Parameter()
                                            .$ref("#/components/parameters/" + BROWSER_LANGUAGE_HEADER)
                            )
                    )
            );
        };
    }
}