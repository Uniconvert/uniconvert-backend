package com.uniconvert.backend.domain.exchange.client;

import com.uniconvert.backend.domain.exchange.client.dto.EcosSearchResponse;
import com.uniconvert.backend.domain.exchange.client.dto.EcosSearchRow;
import com.uniconvert.backend.domain.exchange.enums.EcosItemCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class EcosApiClient {

    private static final String STAT_CODE = "731Y001"; // 주요국 통화의 대원화환율
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RestClient restClient;

    @Value("${ecos.api-key}")
    private String apiKey;

    @Value("${ecos.base-url}")
    private String baseUrl;

    // 최근 7일치 조회 후 그중 최신 날짜 값 반환 (휴장일 대비 여유 있게 조회)
    public Optional<EcosSearchRow> fetchLatestRate(String currencyCode) {
        EcosItemCode itemCode = EcosItemCode.from(currencyCode);
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(7);

        String url = String.format(
                "%s/StatisticSearch/%s/json/kr/1/10/%s/D/%s/%s/%s",
                baseUrl, apiKey, STAT_CODE,
                start.format(DATE_FORMAT), end.format(DATE_FORMAT),
                itemCode.getItemCode()
        );

        try {
            EcosSearchResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(EcosSearchResponse.class);

            List<EcosSearchRow> rows = (response != null && response.statisticSearch() != null)
                    ? response.statisticSearch().row()
                    : null;

            if (rows == null || rows.isEmpty()) {
                return Optional.empty();
            }

            // ECOS는 날짜 오름차순 응답 → 마지막 원소가 최신 날짜
            return Optional.of(rows.get(rows.size() - 1));
        } catch (Exception e) {
            log.warn("ECOS API 호출 실패 - currency: {}, error: {}", currencyCode, e.getMessage());
            return Optional.empty();
        }
    }
    // targetDate 하루만 조회 범위로 ECOS 호출
    public Optional<EcosSearchRow> fetchRateByDate(String currencyCode, LocalDate targetDate) {
        EcosItemCode itemCode = EcosItemCode.from(currencyCode);
        LocalDate end = targetDate;
        LocalDate start = targetDate.minusDays(5);

        String url = String.format(
                "%s/StatisticSearch/%s/json/kr/1/10/%s/D/%s/%s/%s",
                baseUrl, apiKey, STAT_CODE,
                start.format(DATE_FORMAT), end.format(DATE_FORMAT),
                itemCode.getItemCode()
        );

        try {
            EcosSearchResponse response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(EcosSearchResponse.class);

            List<EcosSearchRow> rows = (response != null && response.statisticSearch() != null)
                    ? response.statisticSearch().row()
                    : null;
            if (rows == null || rows.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(rows.get(rows.size() - 1));
        } catch (Exception e) {
            log.warn("ECOS API 호출 실패 - currency: {}, date: {}, error: {}", currencyCode, targetDate, e.getMessage());
            return Optional.empty();
        }
    }
}