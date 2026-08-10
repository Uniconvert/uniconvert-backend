package com.uniconvert.backend.domain.expense.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record MemoDeleteRequest(
        @NotEmpty(message = "삭제할 지출 id 목록이 비어 있습니다.")
        List<Long> expenseIds
) {
}