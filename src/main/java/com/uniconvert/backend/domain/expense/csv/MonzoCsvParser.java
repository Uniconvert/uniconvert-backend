package com.uniconvert.backend.domain.expense.csv;

import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

// Monzo("Monzo Data Export") CSV: Transaction ID,Date,Time,Type,Name,Emoji,Category,Amount,Currency,
//   Local amount,Local currency,Notes and #tags,Address,Receipt,Description,Category split,Money Out,Money In
// 저장 대상: Money Out에 값이 있거나 Amount < 0인 거래 (입금/충전/보너스는 Money In만 존재해 자연히 제외됨)
@Component
public class MonzoCsvParser implements CsvTransactionParser {

    private static final Set<String> REQUIRED_HEADERS =
            Set.of("Transaction ID", "Date", "Time", "Name", "Amount", "Currency", "Money Out", "Money In");

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm:ss");

    @Override
    public boolean supports(List<String> headers) {
        return headers.containsAll(REQUIRED_HEADERS);
    }

    @Override
    public String providerName() {
        return "MONZO";
    }

    @Override
    public ParsedCsvTransaction parseRow(CSVRecord record, int rowNumber) {
        BigDecimal amount = parseAmount(record.get("Amount"));
        String moneyOut = record.get("Money Out").trim();

        if (moneyOut.isEmpty() && amount.signum() >= 0) {
            return null;
        }

        String currency = record.get("Currency").trim().toUpperCase();
        String merchantName = record.get("Name").trim();
        String category = record.isMapped("Category") ? record.get("Category").trim() : null;

        LocalDate date = LocalDate.parse(record.get("Date").trim(), DATE_FORMAT);
        LocalTime time = LocalTime.parse(record.get("Time").trim(), TIME_FORMAT);
        LocalDateTime spentAt = LocalDateTime.of(date, time);

        return new ParsedCsvTransaction(
                rowNumber,
                spentAt,
                merchantName,
                amount.abs(),
                currency,
                CsvCategoryMapper.map(category, merchantName)
        );
    }

    private BigDecimal parseAmount(String raw) {
        return new BigDecimal(raw.trim().replace(",", ""));
    }
}
