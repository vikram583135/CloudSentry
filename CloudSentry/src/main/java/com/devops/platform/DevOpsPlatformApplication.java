package com.devops.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AI-Powered Smart DevOps Incident & Cost Optimization Platform
 * 
 * Main application entry point.
 * 
 * Features:
 * - Real-time metrics ingestion
 * - Anomaly detection engine
 * - Incident auto-creation & management
 * - AI-powered root cause analysis
 * - Cloud cost optimization
 * - Smart alerting & notifications
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class DevOpsPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevOpsPlatformApplication.class, args);
    }
}
