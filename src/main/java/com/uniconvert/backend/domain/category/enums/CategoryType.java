package com.uniconvert.backend.domain.category.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CategoryType {
    FOOD(1L, "식비", "icon_food", 1),
    TRANSPORT(2L, "교통", "icon_transport", 2),
    TUITION(3L, "학비", "icon_tuition", 3),
    HOUSING(4L, "기숙사", "icon_housing", 4),
    SHOPPING(5L, "쇼핑", "icon_shopping", 5),
    MEDICAL(6L, "의료", "icon_medical", 6),
    VISA(7L, "비자", "icon_visa", 7),
    LEISURE(8L, "여가", "icon_leisure", 8),
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
