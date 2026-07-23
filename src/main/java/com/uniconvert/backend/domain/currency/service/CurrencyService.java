package com.uniconvert.backend.domain.currency.service;

import com.uniconvert.backend.domain.currency.dto.response.CurrencyResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CurrencyService {

    public List<CurrencyResponse> getCurrencies() {
        return List.of(
                new CurrencyResponse("KRW", "대한민국 원", "Korean Won", "₩"),
                new CurrencyResponse("USD", "미국 달러", "US Dollar", "$"),
                new CurrencyResponse("EUR", "유로", "Euro", "€"),
                new CurrencyResponse("JPY", "일본 엔", "Japanese Yen", "¥"),
                new CurrencyResponse("CNY", "중국 위안", "Chinese Yuan", "¥")
        );
    }
}