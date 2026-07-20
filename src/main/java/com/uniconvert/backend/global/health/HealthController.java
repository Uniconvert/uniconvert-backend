package com.uniconvert.backend.global.health;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final List<InfrastructureHealthChecker> healthCheckers;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        List<HealthCheckResult> results = healthCheckers.stream()
                .map(InfrastructureHealthChecker::check)
                .toList();

        boolean allUp = results.stream()
                .allMatch(HealthCheckResult::isUp);

        Map<String, HealthCheckResult> checks = results.stream()
                .collect(Collectors.toMap(
                        HealthCheckResult::name,
                        result -> result,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", allUp ? "UP" : "DOWN");
        response.put("service", "uniconvert-backend");
        response.put("timestamp", OffsetDateTime.now().toString());
        response.put("checks", checks);

        return ResponseEntity
                .status(allUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }
}