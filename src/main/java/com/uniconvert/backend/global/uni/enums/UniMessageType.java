package com.uniconvert.backend.global.uni.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "유니 멘트 종류")
public enum UniMessageType {

    /**
     * 화면에 처음 들어갔을 때 우선 표시할 멘트
     */
    ENTRY,

    /**
     * 캐릭터 터치 또는 자동 전환 시 사용할 정적 멘트
     */
    RANDOM,

    /**
     * 실제 사용자 데이터를 이용해 생성한 인사이트
     */
    INSIGHT
}
