package com.uniconvert.backend.global.health;

public interface InfrastructureHealthChecker {

    String name();

    HealthCheckResult check();
}
