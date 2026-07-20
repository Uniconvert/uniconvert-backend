package com.uniconvert.backend.global.config;

import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class SwaggerConfig {

    private static final String BROWSER_LANGUAGE_HEADER = "BrowserLanguageHeader";
    private static final String DRAFT_SPEC_CLASSPATH = "/static/docs/openapi.yaml";

    @Bean
    public OpenAPI openAPI() {
        String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Uniconvert API")
                        .version("v1")
                        .description("Uniconvert backend API documentation")
                        .contact(new Contact().name("Uniconvert")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
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
                                        .schema(new Schema<String>()
                                                .type("string")
                                                .example("ko-KR")
                                                ._default("ko-KR"))
                        ));
    }

    /**
     * static/docs/openapi.yaml(설계 명세 번들, 원본은 docs/openapi/)을 코드 생성 문서에 병합한다.
     * 같은 경로/컴포넌트는 구현(코드 생성) 쪽이 우선하고, 미구현 설계 경로가 추가된다.
     * 언어 헤더 customizer가 병합된 경로에도 적용되도록 먼저 실행한다.
     */
    @Bean
    @Order(1)
    public GlobalOpenApiCustomizer draftSpecMergeCustomizer() {
        return openApi -> {
            OpenAPI draft = loadDraftSpec();
            if (draft == null) {
                return;
            }
            if (draft.getPaths() != null) {
                if (openApi.getPaths() == null) {
                    openApi.setPaths(new Paths());
                }
                draft.getPaths().forEach(openApi.getPaths()::putIfAbsent);
            }
            if (draft.getTags() != null) {
                List<Tag> tags = openApi.getTags() != null ? openApi.getTags() : new ArrayList<>();
                Set<String> existing = tags.stream().map(Tag::getName).collect(Collectors.toSet());
                draft.getTags().stream()
                        .filter(tag -> !existing.contains(tag.getName()))
                        .forEach(tags::add);
                openApi.setTags(tags);
            }
            Components draftComponents = draft.getComponents();
            if (draftComponents != null) {
                Components components = openApi.getComponents() != null ? openApi.getComponents() : new Components();
                components.setSchemas(mergedAbsent(components.getSchemas(), draftComponents.getSchemas()));
                components.setResponses(mergedAbsent(components.getResponses(), draftComponents.getResponses()));
                components.setParameters(mergedAbsent(components.getParameters(), draftComponents.getParameters()));
                components.setSecuritySchemes(
                        mergedAbsent(components.getSecuritySchemes(), draftComponents.getSecuritySchemes()));
                openApi.setComponents(components);
            }
        };
    }

    private OpenAPI loadDraftSpec() {
        try (InputStream in = getClass().getResourceAsStream(DRAFT_SPEC_CLASSPATH)) {
            return in == null ? null : Yaml.mapper().readValue(in, OpenAPI.class);
        } catch (IOException e) {
            return null; // 설계 명세 병합에 실패해도 코드 생성 문서는 그대로 제공한다.
        }
    }

    private static <V> Map<String, V> mergedAbsent(Map<String, V> base, Map<String, V> additions) {
        if (additions == null) {
            return base;
        }
        Map<String, V> merged = base != null ? base : new LinkedHashMap<>();
        additions.forEach(merged::putIfAbsent);
        return merged;
    }

    /**
     * 모든 API 경로에 X-Browser-Language 공통 헤더를 자동으로 노출한다.
     * 컨트롤러마다 @Parameter 어노테이션을 반복 작성하지 않아도 되도록 전역 적용한다.
     */
    @Bean
    @Order(2)
    public GlobalOpenApiCustomizer browserLanguageHeaderCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation ->
                            operation.addParametersItem(
                                    new Parameter().$ref("#/components/parameters/" + BROWSER_LANGUAGE_HEADER)
                            )
                    )
            );
        };
    }
}
