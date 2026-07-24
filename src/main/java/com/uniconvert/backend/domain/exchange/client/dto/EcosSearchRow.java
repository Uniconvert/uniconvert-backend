// EcosSearchRow.java
package com.uniconvert.backend.domain.exchange.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true) // STAT_CODE, ITEM_NAME1 등 안 쓰는 필드 무시
public record EcosSearchRow(
        @JsonProperty("TIME") String time,
        @JsonProperty("DATA_VALUE") String dataValue
) {
}