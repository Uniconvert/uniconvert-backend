package com.uniconvert.backend.domain.exchange.enums;

import com.uniconvert.backend.global.exception.CustomException;
import com.uniconvert.backend.global.exception.ErrorCode;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum EcosItemCode {
    USD("0000001", BigDecimal.ONE),
    EUR("0000003", BigDecimal.ONE),
    JPY("0000002", BigDecimal.valueOf(100)),   // 100엔 단위 — 반드시 100으로 나눠서 정규화
    CNY("0000053", BigDecimal.ONE);            // 0000027(구코드) 아님, 2016년 종료됨

    private final String itemCode;
    private final BigDecimal unitDivisor;

    EcosItemCode(String itemCode, BigDecimal unitDivisor) {
        this.itemCode = itemCode;
        this.unitDivisor = unitDivisor;
    }

    public static EcosItemCode from(String currencyCode) {
        try {
            return valueOf(currencyCode);
        } catch (IllegalArgumentException e) {
            // 사용자에게 그대로 노출되는 문구라 하드코딩 대신 ErrorCode(다국어 메시지 키)로 던진다
            throw new CustomException(ErrorCode.UNSUPPORTED_CURRENCY);
        }
    }
}