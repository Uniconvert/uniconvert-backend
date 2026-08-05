package com.uniconvert.backend.global.uni.dto;

import com.uniconvert.backend.global.uni.enums.UniMessageType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유니 캐릭터 멘트")
public record UniMessageResponse(

        @Schema(
                description = "멘트 식별 키. 같은 멘트의 연속 노출 방지에 사용합니다.",
                example = "EXPENSE_ENTRY_01"
        )
        String key,

        @Schema(
                description = "화면에 표시할 멘트",
                example = "오늘 지출부터 차근히 확인해볼까요?"
        )
        String message,

        @Schema(
                description = "멘트 종류",
                example = "ENTRY"
        )
        UniMessageType type
) {
}
