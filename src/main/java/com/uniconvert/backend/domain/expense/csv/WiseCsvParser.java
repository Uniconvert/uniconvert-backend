package com.uniconvert.backend.domain.expense.csv;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// Wise("account-statement_...") CSV: Type,Product,Started Date,Completed Date,Description,Amount,Fee,Currency,State,Balance
// 저장 대상: Amount < 0 (지출) AND State = COMPLETED (입금/충전은 Amount>=0, 취소는 REVERTED 등으로 자연히 제외됨)
@Component
public class WiseCsvParser implements CsvTransactionParser {

    private static final Set<String> REQUIRED_HEADERS =
            Set.of("Started Date", "Completed Date", "State", "Amount", "Currency", "Description");

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm");

    @Override
    public boolean supports(List<String> headers) {
        return headers.containsAll(REQUIRED_HEADERS);
    }

    @Override
    public String providerName() {
        return "WISE";
    }

    @Override
    public CsvParseResult parse(CSVParser csvParser) {
        List<ParsedCsvTransaction> transactions = new ArrayList<>();
        int total = 0;
        int excluded = 0;

        for (CSVRecord record : csvParser) {
            total++;

            String state = record.get("State").trim();
            BigDecimal amount = parseAmount(record.get("Amount"));

            if (amount.signum() >= 0 || !"COMPLETED".equalsIgnoreCase(state)) {
                excluded++;
                continue;
            }

            String currency = record.get("Currency").trim().toUpperCase();
            String merchantName = record.get("Description").trim();
            LocalDateTime spentAt = LocalDateTime.parse(record.get("Started Date").trim(), DATE_FORMAT);

            transactions.add(new ParsedCsvTransaction(
                    total,
                    spentAt,
                    merchantName,
                    amount.abs(),
                    currency,
                    CsvCategoryMapper.map(null, merchantName)
            ));
        }

        return new CsvParseResult(transactions, total, excluded);
    }

    private BigDecimal parseAmount(String raw) {
        return new BigDecimal(raw.trim().replace(",", ""));
    }
}
