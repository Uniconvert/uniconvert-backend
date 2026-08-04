package com.uniconvert.backend.domain.expense.service;

import com.uniconvert.backend.domain.exchange.dto.response.ConversionResult;
import com.uniconvert.backend.domain.exchange.service.ExchangeRateService;
import com.uniconvert.backend.domain.expense.csv.CsvParseResult;
import com.uniconvert.backend.domain.expense.csv.CsvTransactionParser;
import com.uniconvert.backend.domain.expense.csv.ParsedCsvTransaction;
import com.uniconvert.backend.domain.expense.dto.response.ExpenseImportErrorDetail;
import com.uniconvert.backend.domain.expense.dto.response.ExpenseImportResponse;
import com.uniconvert.backend.domain.expense.entity.Expense;
import com.uniconvert.backend.domain.expense.entity.RateSource;
import com.uniconvert.backend.domain.expense.repository.ExpenseRepository;
import com.uniconvert.backend.domain.user.entity.User;
import com.uniconvert.backend.domain.user.repository.UserRepository;
import com.uniconvert.backend.global.exception.CustomException;
import com.uniconvert.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseImportService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final ExchangeRateService exchangeRateService;
    private final List<CsvTransactionParser> csvTransactionParsers;

    // 환율 캐시 키 — 같은 통화·같은 거래일 행은 exchangeRateService를 딱 한 번만 호출한다
    private record RateKey(String currency, LocalDate date) {
    }

    @Transactional
    public ExpenseImportResponse importCsv(Long userId, MultipartFile file) {
        User user = getUser(userId);

        String content = readText(file);
        if (content.isBlank()) {
            throw new CustomException(ErrorCode.EMPTY_FILE);
        }

        String csvText = stripBom(content);

        CsvTransactionParser parser;
        CsvParseResult parseResult;
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreSurroundingSpaces(true)
                .build();

        try (CSVParser csvParser = format.parse(new StringReader(csvText))) {
            List<String> headers = new ArrayList<>(csvParser.getHeaderNames());
            parser = csvTransactionParsers.stream()
                    .filter(candidate -> candidate.supports(headers))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CSV_FORMAT));

            parseResult = parser.parse(csvParser);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INVALID_CSV_FORMAT);
        }

        List<ParsedCsvTransaction> candidates = parseResult.transactions();

        Map<RateKey, ConversionResult> resolvedRates = new HashMap<>();
        Map<RateKey, String> failedRates = new HashMap<>();
        resolveRates(user, candidates, resolvedRates, failedRates);

        List<Expense> toSave = new ArrayList<>();
        List<ExpenseImportErrorDetail> errors = new ArrayList<>();

        for (ParsedCsvTransaction tx : candidates) {
            RateKey key = new RateKey(tx.originalCurrency(), tx.spentAt().toLocalDate());
            ConversionResult conversion = resolvedRates.get(key);

            if (conversion == null) {
                errors.add(new ExpenseImportErrorDetail(tx.rowNumber(), tx.merchantName(), failedRates.get(key)));
                continue;
            }

            BigDecimal convertedAmountHome = tx.originalAmount()
                    .multiply(conversion.rate())
                    .setScale(4, RoundingMode.HALF_UP);

            toSave.add(new Expense(
                    user,
                    tx.originalAmount(),
                    tx.originalCurrency(),
                    conversion.rate(),
                    RateSource.DAILY_AVERAGE,
                    conversion.rateDate(),
                    convertedAmountHome,
                    tx.merchantName(),
                    normalizeMerchantName(tx.merchantName()),
                    null,
                    tx.spentAt(),
                    null,
                    tx.categoryId()
            ));
        }

        expenseRepository.saveAll(toSave);

        return new ExpenseImportResponse(
                parser.providerName(),
                parseResult.totalRowCount(),
                toSave.size(),
                parseResult.excludedCount(),
                errors.size(),
                errors
        );
    }

    // 같은 통화·거래일 조합은 한 번만 exchangeRateService.getConversionRate()를 호출해 캐싱한다.
    // ECOS가 지원하지 않는 통화 등으로 환율을 못 구하면 임의 환율을 쓰지 않고 실패로 기록만 해둔다.
    private void resolveRates(User user, List<ParsedCsvTransaction> candidates,
                               Map<RateKey, ConversionResult> resolvedRates, Map<RateKey, String> failedRates) {
        Set<RateKey> distinctKeys = candidates.stream()
                .map(tx -> new RateKey(tx.originalCurrency(), tx.spentAt().toLocalDate()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (RateKey key : distinctKeys) {
            try {
                ConversionResult conversion =
                        exchangeRateService.getConversionRate(key.currency(), user.getHomeCurrencyCode(), key.date());
                resolvedRates.put(key, conversion);
            } catch (RuntimeException e) {
                failedRates.put(key, e.getMessage());
            }
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
    }

    private String normalizeMerchantName(String merchantName) {
        return merchantName == null ? null : merchantName.trim().toLowerCase();
    }

    private String readText(MultipartFile file) {
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String stripBom(String text) {
        return text.startsWith("﻿") ? text.substring(1) : text;
    }
}
