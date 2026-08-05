package com.uniconvert.backend.global.uni.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유니 캐릭터가 표시되는 화면")
public enum UniSection {

    EXPENSE,
    POTS,
    REPORT,
    MEMO,
    CALCULATOR
}
