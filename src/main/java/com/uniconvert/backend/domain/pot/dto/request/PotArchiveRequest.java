package com.uniconvert.backend.domain.pot.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pot 보관 상태 변경 요청")
public record PotArchiveRequest(

        @Schema(
                description = """
                        변경할 Pot 보관 상태입니다.
                        true이면 보관 처리하고, false이면 다시 활성화합니다.
                        """,
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        boolean archived
) {
}