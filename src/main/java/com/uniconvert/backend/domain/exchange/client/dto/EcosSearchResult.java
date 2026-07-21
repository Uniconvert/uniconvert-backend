// EcosSearchResult.java
package com.uniconvert.backend.domain.exchange.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EcosSearchResult(
        @JsonProperty("list_total_count") Integer listTotalCount,
        @JsonProperty("row") List<EcosSearchRow> row
) {
}