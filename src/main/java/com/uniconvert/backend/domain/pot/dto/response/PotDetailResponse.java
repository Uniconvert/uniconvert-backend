package com.uniconvert.backend.domain.pot.dto.response;

import com.uniconvert.backend.global.uni.dto.UniMessageBundleResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pot 상세 정보 및 유니 메시지 응답")
public record PotDetailResponse(

        @Schema(description = "Pot 상세 정보")
        PotResponse pot,

        @Schema(description = "Pots 화면 유니 메시지")
        UniMessageBundleResponse uniMessages

) {
}
