package com.uniconvert.backend.domain.category.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CategoryType {
    FOOD(1L, "식비", "icon_food", 1),
    TRANSPORT(2L, "교통", "icon_transport", 2),
    SHOPPING(3L, "쇼핑", "icon_shopping", 3),
    TELECOM(4L, "통신", "icon_telecom", 4),
    ACADEMIC(5L, "학업", "icon_academic", 5),
    TRAVEL(6L, "여행", "icon_travel", 6),
    HOUSING(7L, "주거", "icon_housing", 7),
    SAVINGS(8L, "저축", "icon_savings", 8),
    ETC(9L, "기타", "icon_etc", 9);

    private final Long categoryId;
    private final String displayName;
    private final String iconKey;
    private final int sortOrder;

    CategoryType(Long categoryId, String displayName, String iconKey, int sortOrder) {
        this.categoryId = categoryId;
        this.displayName = displayName;
        this.iconKey = iconKey;
        this.sortOrder = sortOrder;
    }

    // expense 도메인에서 지출 등록 시 categoryId 유효성 검사용으로 사용 예정
    public static boolean isValid(Long categoryId) {
        return Arrays.stream(values())
                .anyMatch(type -> type.categoryId.equals(categoryId));
    }

    public static CategoryType fromId(Long categoryId) {
        return Arrays.stream(values())
                .filter(type -> type.categoryId.equals(categoryId))
                .findFirst()
                .orElse(null);
    }
}
