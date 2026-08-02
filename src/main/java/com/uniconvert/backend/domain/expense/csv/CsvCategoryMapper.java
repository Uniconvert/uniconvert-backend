package com.uniconvert.backend.domain.expense.csv;

import com.uniconvert.backend.domain.category.enums.CategoryType;

import java.util.List;
import java.util.Locale;
import java.util.Map;

// CSV에는 우리 서비스의 CategoryType(1~9)이 없으므로 최선 추정으로 매핑한다.
// 1) Monzo의 Category 텍스트를 우선 사용하고, 2) 없거나 못 찾으면 가맹점명 키워드로 추정하고,
// 3) 그래도 못 찾으면 ETC로 분류한다. 매칭 실패가 오류 처리 대상은 아니다(요구사항의 "환율 오류"와는 별개).
public final class CsvCategoryMapper {

    private CsvCategoryMapper() {
    }

    private static final Map<String, Long> MONZO_CATEGORY_MAP = Map.ofEntries(
            Map.entry("eating out", CategoryType.FOOD.getCategoryId()),
            Map.entry("groceries", CategoryType.FOOD.getCategoryId()),
            Map.entry("transport", CategoryType.TRANSPORT.getCategoryId()),
            Map.entry("shopping", CategoryType.SHOPPING.getCategoryId()),
            Map.entry("bills", CategoryType.HOUSING.getCategoryId()),
            Map.entry("entertainment", CategoryType.ETC.getCategoryId()),
            Map.entry("holidays", CategoryType.TRAVEL.getCategoryId()),
            Map.entry("finances", CategoryType.SAVINGS.getCategoryId()),
            Map.entry("charity", CategoryType.ETC.getCategoryId()),
            Map.entry("personal care", CategoryType.ETC.getCategoryId()),
            Map.entry("family", CategoryType.ETC.getCategoryId()),
            Map.entry("gifts", CategoryType.ETC.getCategoryId()),
            Map.entry("general", CategoryType.ETC.getCategoryId())
    );

    private record KeywordCategory(Long categoryId, List<String> keywords) {
    }

    private static final List<KeywordCategory> MERCHANT_KEYWORDS = List.of(
            new KeywordCategory(CategoryType.FOOD.getCategoryId(), List.of(
                    "coffee", "cafe", "café", "restaurant", "starbucks", "food", "market", "grocery",
                    "lidl", "mercadona", "sainsbury", "tesco", "fruteria", "heladeria", "poundland",
                    "deliveroo", "vending"
            )),
            new KeywordCategory(CategoryType.TRANSPORT.getCategoryId(), List.of(
                    "uber", "taxi", "transport", "train", "bus", "tfl", "metro", "renfe"
            )),
            new KeywordCategory(CategoryType.TRAVEL.getCategoryId(), List.of(
                    "museum", "hotel", "airbnb", "airlines", "flight", "airport"
            )),
            new KeywordCategory(CategoryType.TELECOM.getCategoryId(), List.of(
                    "vodafone", "telecom", "mobile"
            ))
    );

    public static Long map(String rawCategory, String merchantName) {
        if (rawCategory != null && !rawCategory.isBlank()) {
            Long mapped = MONZO_CATEGORY_MAP.get(rawCategory.trim().toLowerCase(Locale.ROOT));
            if (mapped != null) {
                return mapped;
            }
        }

        if (merchantName != null && !merchantName.isBlank()) {
            String normalized = merchantName.toLowerCase(Locale.ROOT);
            for (KeywordCategory keywordCategory : MERCHANT_KEYWORDS) {
                if (keywordCategory.keywords().stream().anyMatch(normalized::contains)) {
                    return keywordCategory.categoryId();
                }
            }
        }

        return CategoryType.ETC.getCategoryId();
    }
}
