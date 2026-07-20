package com.uniconvert.backend.global.health;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DatabaseHealthChecker implements InfrastructureHealthChecker {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public String name() {
        return "database";
    }

    @Override
    public HealthCheckResult check() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            if (result != null && result == 1) {
                return HealthCheckResult.up(
                        name(),
                        "Database connection is OK",
                        Map.of("query", "SELECT 1")
                );
            }

            return HealthCheckResult.down(
                    name(),
                    "Database returned unexpected result",
                    Map.of("result", String.valueOf(result))
            );
        } catch (Exception e) {
            return HealthCheckResult.down(
                    name(),
                    e.getClass().getSimpleName() + ": " + e.getMessage(),
                    Map.of()
            );
        }
    }
}