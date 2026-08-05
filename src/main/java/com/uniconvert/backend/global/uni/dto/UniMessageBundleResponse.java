package com.uniconvert.backend.global.uni.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "화면별 유니 멘트 모음")
public record UniMessageBundleResponse(

        @Schema(
                description = "화면 최초 진입 시 사용할 필수 멘트 2개"
        )
        List<UniMessageResponse> entryMessages,

        @Schema(
                description = """
                        캐릭터 터치 또는 자동 전환 시 사용할 멘트 목록입니다.
                        정적 랜덤 멘트와 데이터 기반 인사이트가 함께 포함될 수 있습니다.
                        """
        )
        List<UniMessageResponse> randomMessages
) {
}
