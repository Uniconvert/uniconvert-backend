package com.uniconvert.backend.global.uni.service;

import com.uniconvert.backend.global.uni.enums.UniMessageType;
import com.uniconvert.backend.global.uni.enums.UniSection;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class UniMessageCatalog {

    private static final Map<UniSection, List<UniMessageDefinition>>
            ENTRY_MESSAGES = new EnumMap<>(UniSection.class);

    private static final Map<UniSection, List<UniMessageDefinition>>
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

    public static List<UniMessageDefinition> getEntryMessages(
            UniSection section
    ) {
        return ENTRY_MESSAGES.getOrDefault(
                section,
                List.of()
        );
    }

    public static List<UniMessageDefinition> getRandomMessages(
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
                        entry("EXPENSE_ENTRY_01"),
                        entry("EXPENSE_ENTRY_02")
                )
        );

        RANDOM_MESSAGES.put(
                UniSection.EXPENSE,
                List.of(
                        random("EXPENSE_RANDOM_01"),
                        random("EXPENSE_RANDOM_02"),
                        random("EXPENSE_RANDOM_03"),
                        random("EXPENSE_RANDOM_04"),
                        random("EXPENSE_RANDOM_05"),
                        random("EXPENSE_RANDOM_06"),
                        random("EXPENSE_RANDOM_07"),
                        random("EXPENSE_RANDOM_08"),
                        random("EXPENSE_RANDOM_09"),
                        random("EXPENSE_RANDOM_10")
                )
        );
    }

    private static void registerPotMessages() {
        ENTRY_MESSAGES.put(
                UniSection.POTS,
                List.of(
                        entry("POTS_ENTRY_01"),
                        entry("POTS_ENTRY_02")
                )
        );

        RANDOM_MESSAGES.put(
                UniSection.POTS,
                List.of(
                        random("POTS_RANDOM_01"),
                        random("POTS_RANDOM_02"),
                        random("POTS_RANDOM_03"),
                        random("POTS_RANDOM_04"),
                        random("POTS_RANDOM_05"),
                        random("POTS_RANDOM_06"),
                        random("POTS_RANDOM_07"),
                        random("POTS_RANDOM_08"),
                        random("POTS_RANDOM_09"),
                        random("POTS_RANDOM_10")
                )
        );
    }

    private static void registerReportMessages() {
        ENTRY_MESSAGES.put(
                UniSection.REPORT,
                List.of(
                        entry("REPORT_ENTRY_01"),
                        entry("REPORT_ENTRY_02")
                )
        );

        RANDOM_MESSAGES.put(
                UniSection.REPORT,
                List.of(
                        random("REPORT_RANDOM_01"),
                        random("REPORT_RANDOM_02"),
                        random("REPORT_RANDOM_03"),
                        random("REPORT_RANDOM_04"),
                        random("REPORT_RANDOM_05"),
                        random("REPORT_RANDOM_06"),
                        random("REPORT_RANDOM_07"),
                        random("REPORT_RANDOM_08"),
                        random("REPORT_RANDOM_09"),
                        random("REPORT_RANDOM_10")
                )
        );
    }

    private static void registerMemoMessages() {
        ENTRY_MESSAGES.put(
                UniSection.MEMO,
                List.of(
                        entry("MEMO_ENTRY_01"),
                        entry("MEMO_ENTRY_02")
                )
        );

        RANDOM_MESSAGES.put(
                UniSection.MEMO,
                List.of(
                        random("MEMO_RANDOM_01"),
                        random("MEMO_RANDOM_02"),
                        random("MEMO_RANDOM_03"),
                        random("MEMO_RANDOM_04"),
                        random("MEMO_RANDOM_05"),
                        random("MEMO_RANDOM_06"),
                        random("MEMO_RANDOM_07"),
                        random("MEMO_RANDOM_08"),
                        random("MEMO_RANDOM_09"),
                        random("MEMO_RANDOM_10")
                )
        );
    }

    private static void registerCalculatorMessages() {
        ENTRY_MESSAGES.put(
                UniSection.CALCULATOR,
                List.of(
                        entry("CALCULATOR_ENTRY_01"),
                        entry("CALCULATOR_ENTRY_02")
                )
        );

        RANDOM_MESSAGES.put(
                UniSection.CALCULATOR,
                List.of(
                        random("CALCULATOR_RANDOM_01"),
                        random("CALCULATOR_RANDOM_02"),
                        random("CALCULATOR_RANDOM_03"),
                        random("CALCULATOR_RANDOM_04"),
                        random("CALCULATOR_RANDOM_05"),
                        random("CALCULATOR_RANDOM_06"),
                        random("CALCULATOR_RANDOM_07"),
                        random("CALCULATOR_RANDOM_08"),
                        random("CALCULATOR_RANDOM_09"),
                        random("CALCULATOR_RANDOM_10")
                )
        );
    }

    private static UniMessageDefinition entry(String key) {
        return new UniMessageDefinition(
                key,
                UniMessageType.ENTRY
        );
    }

    private static UniMessageDefinition random(String key) {
        return new UniMessageDefinition(
                key,
                UniMessageType.RANDOM
        );
    }

    public record UniMessageDefinition(
            String key,
            UniMessageType type
    ) {
    }
}
