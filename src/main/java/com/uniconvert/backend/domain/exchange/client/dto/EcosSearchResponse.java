// EcosSearchResponse.java
package com.uniconvert.backend.domain.exchange.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EcosSearchResponse(
        @JsonProperty("StatisticSearch") EcosSearchResult statisticSearch
) {
}