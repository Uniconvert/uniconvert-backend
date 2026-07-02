package com.uniconvert.backend.domain.currency.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "currency")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Currency {

    @Id
    @Column(name = "code", length = 3, nullable = false)
    private String code;

    @Column(name = "name_ko", nullable = false, length = 50)
    private String nameKo;

    @Column(name = "name_en", nullable = false, length = 50)
    private String nameEn;

    @Column(nullable = false, length = 5)
    private String symbol;

    @Column(name = "is_supported", nullable = false)
    private boolean isSupported;

    @Builder
    private Currency(String code, String nameKo, String nameEn, String symbol, boolean isSupported) {
        this.code = code;
        this.nameKo = nameKo;
        this.nameEn = nameEn;
        this.symbol = symbol;
        this.isSupported = isSupported;
    }
}
