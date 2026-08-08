package com.uniconvert.backend.domain.expense.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MonzoCsvParserTest {

    private static final String HEADER =
            "Transaction ID,Date,Time,Type,Name,Emoji,Category,Amount,Currency,Local amount,Local currency,"
                    + "Notes and #tags,Address,Receipt,Description,Category split,Money Out,Money In";

    // 정상 행
    private static final String VALID_ROW =
            "tx_1,20/02/2026,0:17:12,Card payment,Deliveroo,,Eating out,-14.07,GBP,-14.07,GBP,,1 Cousin Lane,,DELIVEROO,,-14.07,";

    // Emoji 열이 깨져 Category와 합쳐진 행 — Amount 위치에 통화코드(GBP)가 들어와 예전엔 500이 났다
    private static final String BROKEN_ROW =
            "tx_2,26/02/2026,13:58:57,Card payment,Starbucks,??Eating out,-10.35,GBP,-10.35,GBP,,Stocker Road,,Starbucks,,-10.35,,";

    @Test
    void 깨진_행은_500이_아니라_errors로_모인다() throws IOException {
        CsvParseResult result = parse(HEADER + "\n" + VALID_ROW + "\n" + BROKEN_ROW + "\n");

        assertEquals(2, result.totalRowCount());
        assertEquals(1, result.transactions().size());
        assertEquals(List.of(2), result.invalidRowNumbers());
    }

    private CsvParseResult parse(String csvText) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreSurroundingSpaces(true)
                .build();

        try (CSVParser csvParser = format.parse(new StringReader(csvText))) {
            return new MonzoCsvParser().parse(csvParser);
        }
    }
}
