package com.uniconvert.backend.global.uni.service;

import com.uniconvert.backend.global.uni.dto.UniMessageResponse;
import com.uniconvert.backend.global.uni.enums.UniMessageType;
import com.uniconvert.backend.global.uni.enums.UniSection;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class UniMessageCatalog {

    private static final Map<UniSection, List<UniMessageResponse>>
            ENTRY_MESSAGES = new EnumMap<>(UniSection.class);

    private static final Map<UniSection, List<UniMessageResponse>>
            RANDOM_MESSAGES = new EnumMap<>(UniSection.class);

    static {
        registerExpenseMessages();
        registerPotMessages();
        registerReportMessages();
        registerMemoMessages();
        registerCalculatorMessages();
    }

    private UniMessageCatalog() {
    }

    public static List<UniMessageResponse> getEntryMessages(
            UniSection section
    ) {
        return ENTRY_MESSAGES.getOrDefault(
                section,
                List.of()
        );
    }

    public static List<UniMessageResponse> getRandomMessages(
            UniSection section
    ) {
        return RANDOM_MESSAGES.getOrDefault(
                section,
                List.of()
        );
    }

    private static void registerExpenseMessages() {
        ENTRY_MESSAGES.put(
                UniSection.EXPENSE,
                List.of(
                        entry(
                                "EXPENSE_ENTRY_01",
                                "오늘 지출부터 차근히 확인해볼까요?"
                        ),
                        entry(
                                "EXPENSE_ENTRY_02",
                                "카테고리별 흐름을 보면 조정할 지점이 더 잘 보여요."
                        )
                )
        );

        RANDOM_MESSAGES.put(
                UniSection.EXPENSE,
                List.of(
                        random(
                                "EXPENSE_RANDOM_01",
                                "큰 금액부터 확인하면 지출 흐름을 빠르게 파악할 수 있어요."
                        ),
                        random(
                                "EXPENSE_RANDOM_02",
                                "반복되는 소액 결제도 한 달 동안 모이면 생각보다 커질 수 있어요."
                        ),
                        random(
                                "EXPENSE_RANDOM_03",
                                "결제 직후 기록해두면 빠뜨리는 지출을 줄일 수 있어요."
                        ),
                        random(
                                "EXPENSE_RANDOM_04",
                                "필요한 지출과 선택한 지출을 나눠서 살펴보는 것도 좋아요."
                        ),
                        random(
                                "EXPENSE_RANDOM_05",
                                "카테고리를 정확히 지정하면 다음 달 분석이 더 쉬워져요."
                        ),
                        random(
                                "EXPENSE_RANDOM_06",
                                "하루 지출보다 일주일 평균을 보면 소비 흐름이 더 잘 보여요."
                        ),
                        random(
                                "EXPENSE_RANDOM_07",
                                "예산은 남은 금액뿐 아니라 사용하는 속도도 함께 봐야 해요."
                        ),
                        random(
                                "EXPENSE_RANDOM_08",
                                "갑자기 커진 카테고리가 있는지 한 번 확인해보세요."
                        ),
                        random(
                                "EXPENSE_RANDOM_09",
                                "한 번에 크게 줄이기보다 반복 지출 하나부터 조정해봐요."
                        ),
                        random(
                                "EXPENSE_RANDOM_10",
                                "지출 이유를 메모하면 나중에 판단하기가 훨씬 쉬워져요."
                        )
                )
        );
    }

    private static void registerPotMessages() {
        ENTRY_MESSAGES.put(
                UniSection.POTS,
                List.of(
                        entry(
                                "POTS_ENTRY_01",
                                "목표 금액과 현재 모인 금액을 먼저 확인해볼게요."
                        ),
                        entry(
                                "POTS_ENTRY_02",
                                "계획한 금액대로 꾸준히 쌓이고 있는지 살펴봐요."
                        )
                )
        );

        RANDOM_MESSAGES.put(
                UniSection.POTS,
                List.of(
                        random(
                                "POTS_RANDOM_01",
                                "목표를 작게 나누면 매달 실행하기가 더 쉬워져요."
                        ),
                        random(
                                "POTS_RANDOM_02",
                                "큰 금액보다 꾸준한 배정이 목표 달성에 더 중요해요."
                        ),
                        random(
                                "POTS_RANDOM_03",
                                "이번 달 배정 금액이 계획과 맞는지 확인해보세요."
                        ),
                        random(
                                "POTS_RANDOM_04",
                                "목표가 여러 개라면 우선순위를 정해두는 것이 좋아요."
                        ),
                        random(
                                "POTS_RANDOM_05",
                                "현재 생활비에 부담이 없는 범위에서 계획을 조정해봐요."
                        ),
                        random(
                                "POTS_RANDOM_06",
                                "조금씩 쌓인 금액도 계속 기록하면 분명한 성과가 돼요."
                        ),
                        random(
                                "POTS_RANDOM_07",
                                "목표 금액이 너무 멀게 느껴지면 월 계획 금액부터 점검해봐요."
                        ),
                        random(
                                "POTS_RANDOM_08",
                                "완료한 Pot은 보관하고 다음 목표를 정리할 수 있어요."
                        ),
                        random(
                                "POTS_RANDOM_09",
                                "이번 달 여유 금액을 확인한 뒤 추가 배정을 결정해봐요."
                        ),
                        random(
                                "POTS_RANDOM_10",
                                "목표별 진행률을 비교하면 우선할 Pot을 정하기 쉬워져요."
                        )
                )
        );
    }

    private static void registerReportMessages() {
        ENTRY_MESSAGES.put(
                UniSection.REPORT,
                List.of(
                        entry(
                                "REPORT_ENTRY_01",
                                "이번 달 지출 흐름을 숫자로 정리해봤어요."
                        ),
                        entry(
                                "REPORT_ENTRY_02",
                                "변화가 큰 항목부터 확인하면 관리가 쉬워져요."
                        )
                )
        );

        RANDOM_MESSAGES.put(
                UniSection.REPORT,
                List.of(
                        random(
                                "REPORT_RANDOM_01",
                                "총액만 보지 말고 카테고리별 비중도 함께 확인해보세요."
                        ),
                        random(
                                "REPORT_RANDOM_02",
                                "특정 날짜의 큰 지출이 월 전체 흐름에 영향을 줄 수 있어요."
                        ),
                        random(
                                "REPORT_RANDOM_03",
                                "지난달과 비교하면 이번 달의 변화가 더 분명하게 보여요."
                        ),
                        random(
                                "REPORT_RANDOM_04",
                                "금액과 비율을 함께 보면 왜 변했는지 이해하기 쉬워져요."
                        ),
                        random(
                                "REPORT_RANDOM_05",
                                "고정적으로 발생하는 지출과 일시적인 지출을 구분해보세요."
                        ),
                        random(
                                "REPORT_RANDOM_06",
                                "하루 평균 지출을 확인하면 남은 기간을 계획하기 좋아요."
                        ),
                        random(
                                "REPORT_RANDOM_07",
                                "월말에 급격히 늘어나는 지출이 있는지도 살펴보세요."
                        ),
                        random(
                                "REPORT_RANDOM_08",
                                "변화가 크지 않다면 현재 소비 패턴이 안정적이라는 뜻일 수 있어요."
                        ),
                        random(
                                "REPORT_RANDOM_09",
                                "지출이 늘어난 이유를 알면 다음 달 계획도 더 정확해져요."
                        ),
                        random(
                                "REPORT_RANDOM_10",
                                "리포트는 결과보다 다음 행동을 정하는 데 활용하는 것이 좋아요."
                        )
                )
        );
    }

    private static void registerMemoMessages() {
        ENTRY_MESSAGES.put(
                UniSection.MEMO,
                List.of(
                        entry(
                                "MEMO_ENTRY_01",
                                "남겨둔 메모를 보면 지출 이유를 다시 확인할 수 있어요."
                        ),
                        entry(
                                "MEMO_ENTRY_02",
                                "기록이 쌓일수록 소비 패턴이 더 분명해져요."
                        )
                )
        );

        RANDOM_MESSAGES.put(
                UniSection.MEMO,
                List.of(
                        random(
                                "MEMO_RANDOM_01",
                                "상점명과 지출 이유를 함께 적어두면 나중에 찾기 쉬워요."
                        ),
                        random(
                                "MEMO_RANDOM_02",
                                "메모는 길게 쓰기보다 핵심 이유만 남겨도 충분해요."
                        ),
                        random(
                                "MEMO_RANDOM_03",
                                "예상하지 못한 지출에는 이유를 적어두는 것이 좋아요."
                        ),
                        random(
                                "MEMO_RANDOM_04",
                                "반복되는 메모가 있다면 고정적인 소비 습관일 수 있어요."
                        ),
                        random(
                                "MEMO_RANDOM_05",
                                "충동적으로 결제한 항목은 다음에 다시 확인해보세요."
                        ),
                        random(
                                "MEMO_RANDOM_06",
                                "특별한 일정이 있었던 날은 상황도 함께 적어두면 좋아요."
                        ),
                        random(
                                "MEMO_RANDOM_07",
                                "짧은 기록도 시간이 지나면 유용한 판단 근거가 돼요."
                        ),
                        random(
                                "MEMO_RANDOM_08",
                                "같은 상점의 메모를 모아보면 반복 지출을 찾기 쉬워요."
                        ),
                        random(
                                "MEMO_RANDOM_09",
                                "일주일에 한 번 메모를 검토하면 소비 이유를 정리하기 좋아요."
                        ),
                        random(
                                "MEMO_RANDOM_10",
                                "기록하지 않은 기억보다 기록된 한 줄이 더 정확해요."
                        )
                )
        );
    }

    private static void registerCalculatorMessages() {
        ENTRY_MESSAGES.put(
                UniSection.CALCULATOR,
                List.of(
                        entry(
                                "CALCULATOR_ENTRY_01",
                                "금액과 통화를 입력하면 현재 기준으로 계산해드릴게요."
                        ),
                        entry(
                                "CALCULATOR_ENTRY_02",
                                "결제 전 환산 금액을 확인하면 예산 관리가 더 정확해져요."
                        )
                )
        );

        RANDOM_MESSAGES.put(
                UniSection.CALCULATOR,
                List.of(
                        random(
                                "CALCULATOR_RANDOM_01",
                                "환율은 변할 수 있으니 큰 금액은 결제 전에 다시 확인해보세요."
                        ),
                        random(
                                "CALCULATOR_RANDOM_02",
                                "출발 통화와 도착 통화의 방향을 먼저 확인해주세요."
                        ),
                        random(
                                "CALCULATOR_RANDOM_03",
                                "금액이 클수록 작은 환율 차이도 최종 금액에 영향을 줘요."
                        ),
                        random(
                                "CALCULATOR_RANDOM_04",
                                "여행 예산을 정할 때도 예상 환산 금액을 활용할 수 있어요."
                        ),
                        random(
                                "CALCULATOR_RANDOM_05",
                                "Pots 목표 금액을 정하기 전에 현지 통화로 계산해보는 것도 좋아요."
                        ),
                        random(
                                "CALCULATOR_RANDOM_06",
                                "주말이나 공휴일에는 가장 최근 영업일 환율이 사용될 수 있어요."
                        ),
                        random(
                                "CALCULATOR_RANDOM_07",
                                "소수점 차이가 있어도 여러 번 결제하면 누적될 수 있어요."
                        ),
                        random(
                                "CALCULATOR_RANDOM_08",
                                "같은 금액도 기준 통화가 바뀌면 결과가 달라져요."
                        ),
                        random(
                                "CALCULATOR_RANDOM_09",
                                "실제 청구액은 카드사 적용 시점이나 수수료에 따라 달라질 수 있어요."
                        ),
                        random(
                                "CALCULATOR_RANDOM_10",
                                "계산 결과는 지출 등록 전 확인용으로 활용하면 좋아요."
                        )
                )
        );
    }

    private static UniMessageResponse entry(
            String key,
            String message
    ) {
        return new UniMessageResponse(
                key,
                message,
                UniMessageType.ENTRY
        );
    }

    private static UniMessageResponse random(
            String key,
            String message
    ) {
        return new UniMessageResponse(
                key,
                message,
                UniMessageType.RANDOM
        );
    }
}
