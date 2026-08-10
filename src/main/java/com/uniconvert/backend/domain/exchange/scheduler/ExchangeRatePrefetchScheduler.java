package com.uniconvert.backend.domain.exchange.scheduler;

import com.uniconvert.backend.domain.exchange.enums.EcosItemCode;
import com.uniconvert.backend.domain.exchange.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매일 아침 지원 통화 전체의 오늘자 환율을 미리 조회·저장한다.
 *
 * 배경: getCurrentRate()는 "누군가 요청할 때"만 ECOS를 호출하는 lazy 구조라,
 * 서비스를 며칠~몇 주간 아무도 쓰지 않으면 그 기간만큼 DB에 새 값이 안 쌓인다.
 * 그 상태에서 첫 사용자가 들어와 지출을 등록하면, 실제로는 며칠/몇 주 전
 * 환율값이 "가장 최근값"으로 잘못 적용될 수 있다 (fallback 자체는 정상 동작이지만,
 * 그 fallback이 가리키는 값이 지나치게 오래된 경우가 문제).
 *
 * 이 스케줄러는 매일 아침 한국은행 환율 고시 이후 시각에 지원 통화 전체를
 * 미리 조회해 DB에 채워둠으로써, 사용자 요청 시점엔 항상 당일 값이 준비돼
 * 있도록 한다. 이미 오늘자 값이 저장돼 있으면 getCurrentRate() 내부에서
 * ECOS를 다시 호출하지 않으므로 중복 저장 걱정은 없다.
 *
 * 주말·공휴일은 ECOS가 값을 안 줄 수 있는데, 이 경우 getCurrentRate()는
 * DB의 가장 최근값으로 fallback하거나(값이 있으면) NOT_FOUND를 던진다.
 * 스케줄러는 사용자 요청 흐름과 무관하므로, 통화별로 실패해도 예외를
 * 삼키고 다음 통화로 넘어간다 — 한 통화 실패가 나머지 통화 저장을
 * 막아서는 안 된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRatePrefetchScheduler {

    private final ExchangeRateService exchangeRateService;

    // 매일 09:05 (평일/주말 구분 없이 실행 — 주말·공휴일은 ECOS가 알아서 값을 안 줌)
    // 한국은행 환율 고시가 통상 오전 중 이뤄지므로 09:00 이후로 여유 있게 설정
    @Scheduled(cron = "0 5 9 * * *")
    public void prefetchTodayRates() {
        log.info("[ExchangeRatePrefetch] 오늘자 환율 사전 저장 시작");

        for (EcosItemCode itemCode : EcosItemCode.values()) {
            String currencyCode = itemCode.name();
            try {
                exchangeRateService.getCurrentRate(currencyCode);
                log.info("[ExchangeRatePrefetch] {} 환율 저장 완료", currencyCode);
            } catch (Exception e) {
                // 휴장일 등으로 실패해도 다른 통화 저장은 계속 진행
                log.warn("[ExchangeRatePrefetch] {} 환율 저장 실패 — 휴장일 등으로 값이 없을 수 있음: {}",
                        currencyCode, e.getMessage());
            }
        }

        log.info("[ExchangeRatePrefetch] 오늘자 환율 사전 저장 종료");
    }
}