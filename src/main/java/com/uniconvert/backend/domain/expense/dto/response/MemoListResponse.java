package com.uniconvert.backend.domain.expense.dto.response;

import com.uniconvert.backend.global.uni.dto.UniMessageBundleResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

@Schema(description = "메모 목록 및 유니 메시지 응답")
public record MemoListResponse(

        @Schema(description = "메모 목록")
        Page<MemoListItemResponse> memos,

        @Schema(description = "메모 화면 유니 메시지")
        UniMessageBundleResponse uniMessages

) {
}