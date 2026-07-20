package com.uniconvert.backend.global.health;

import java.util.Map;

public record HealthCheckResult(
        String name,
        String status,
        String message,
        Map<String, Object> details
) {
    public static HealthCheckResult up(String name, String message, Map<String, Object> details) {
        return new HealthCheckResult(name, "UP", message, details);
    }

    public static HealthCheckResult down(String name, String message, Map<String, Object> details) {
        return new HealthCheckResult(name, "DOWN", message, details);
    }

    public boolean isUp() {
        return "UP".equals(status);
    }
}
