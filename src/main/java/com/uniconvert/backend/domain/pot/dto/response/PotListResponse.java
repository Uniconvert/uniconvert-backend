package com.uniconvert.backend.domain.pot.dto.response;

import com.uniconvert.backend.global.uni.dto.UniMessageBundleResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Pot 목록 및 유니 메시지 응답")
public record PotListResponse(

        @Schema(description = "Pot 목록")
        List<PotResponse> pots,

        @Schema(description = "Pots 화면 유니 메시지")
        UniMessageBundleResponse uniMessages

) {
}