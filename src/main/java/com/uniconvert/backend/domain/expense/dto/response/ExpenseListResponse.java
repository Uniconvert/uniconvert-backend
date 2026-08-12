package com.uniconvert.backend.domain.expense.dto.response;

import com.uniconvert.backend.global.uni.dto.UniMessageBundleResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

@Schema(description = "지출 목록 및 유니 메시지 응답")
public record ExpenseListResponse(

        @Schema(description = "지출 목록")
        Page<ExpenseListItemResponse> expenses,

        @Schema(description = "지출 화면 유니 메시지")
        UniMessageBundleResponse uniMessages

) {
}
