package com.devops.platform.rca.engine;

import com.devops.platform.analyzer.model.Anomaly;
import com.devops.platform.incident.model.Incident;
import com.devops.platform.rca.model.ConfidenceLevel;
import com.devops.platform.rca.model.PatternType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Pattern matcher for historical incident comparison.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PatternMatcher {

    private final RuleEngine ruleEngine;

    /**
     * Generates suggested actions based on pattern type.
     */
    public List<String> getSuggestedActions(PatternType patternType) {
        return switch (patternType) {
            case RESOURCE_EXHAUSTION -> List.of(
                    "Check current resource usage (CPU/Memory/Disk)",
                    "Identify resource-heavy processes",
                    "Scale up resources or add more instances",
                    "Review recent deployments for resource leaks",
                    "Consider implementing resource limits");
            case CONNECTION_ISSUE -> List.of(
                    "Check database connection pool status",
                    "Verify network connectivity to dependencies",
                    "Review connection timeout settings",
                    "Check for firewall rule changes",
                    "Verify DNS resolution");
            case CONFIGURATION_ERROR -> List.of(
                    "Review recent configuration changes",
                    "Verify environment variables are set correctly",
                    "Check for missing or invalid config files",
                    "Compare with known working configuration",
                    "Roll back to previous configuration if needed");
            case DEPLOYMENT_RELATED -> List.of(
                    "Review recent deployment logs",
                    "Compare metrics before and after deployment",
                    "Check for missing dependencies",
                    "Verify deployment configuration",
                    "Consider rolling back to previous version");
            case DEPENDENCY_FAILURE -> List.of(
                    "Check status of downstream services",
                    "Verify API endpoint availability",
                    "Review error logs from dependent services",
                    "Implement circuit breaker pattern",
                    "Consider fallback mechanisms");
            case TRAFFIC_SPIKE -> List.of(
                    "Enable auto-scaling if available",
                    "Review traffic patterns for anomalies",
                    "Check for potential DDoS attack",
                    "Implement rate limiting",
                    "Consider caching strategies");
            case MEMORY_LEAK -> List.of(
                    "Capture heap dump for analysis",
                    "Review recent code changes affecting memory",
                    "Check for unclosed connections/resources",
                    "Increase heap size temporarily",
                    "Schedule pod/service restart");
            case DISK_IO_BOTTLENECK -> List.of(
                    "Check disk I/O wait times",
                    "Identify processes with high disk usage",
                    "Consider SSD upgrade or faster storage",
                    "Review and optimize database queries",
                    "Implement caching for frequently accessed data");
            case NETWORK_LATENCY -> List.of(
                    "Check network bandwidth utilization",
                    "Verify no packet loss on network path",
                    "Review DNS resolution times",
                    "Check for network saturation",
                    "Consider CDN for static content");
            case DATABASE_DEADLOCK -> List.of(
                    "Review database lock status",
                    "Identify blocking queries",
                    "Optimize transaction scope",
                    "Consider query optimization",
                    "Review database connection pool settings");
            case CACHE_MISS -> List.of(
                    "Check cache hit ratio",
                    "Review cache eviction policies",
                    "Increase cache size if needed",
                    "Verify cache warmup is working",
                    "Check for cache invalidation issues");
            case RATE_LIMITING -> List.of(
                    "Review API rate limit thresholds",
                    "Identify clients exceeding limits",
                    "Implement request queuing",
                    "Consider tiered rate limiting",
                    "Contact API provider if external");
            case CERTIFICATE_EXPIRY -> List.of(
                    "Check certificate expiration dates",
                    "Renew expiring certificates",
                    "Verify certificate chain is complete",
                    "Update certificate in all environments",
                    "Set up certificate monitoring alerts");
            case DNS_RESOLUTION -> List.of(
                    "Verify DNS server availability",
                    "Check for DNS propagation issues",
                    "Review recent DNS changes",
                    "Consider using DNS caching",
                    "Check /etc/resolv.conf configuration");
            case AUTHENTICATION_FAILURE -> List.of(
                    "Verify credentials and tokens",
                    "Check for expired API keys",
                    "Review IAM/RBAC permissions",
                    "Check for account lockouts",
                    "Verify OAuth token refresh logic");
            default -> List.of(
                    "Review application logs for errors",
                    "Check monitoring dashboards",
                    "Compare with baseline metrics",
                    "Engage subject matter experts",
                    "Document findings for post-mortem");
        };
    }

    /**
     * Generates a root cause summary based on anomaly and pattern.
     */
    public String generateRootCauseSummary(Anomaly anomaly, PatternType patternType) {
        String metricContext = String.format("%s = %.2f",
                anomaly.getMetricType(), anomaly.getCurrentValue());

        return switch (patternType) {
            case RESOURCE_EXHAUSTION -> String.format(
                    "Resource exhaustion detected. %s indicates system resources are critically low. " +
                            "This may lead to service degradation or outages.",
                    metricContext);
            case CONNECTION_ISSUE -> String.format(
                    "Connection issues detected. %s suggests problems with database or external service connections. " +
                            "Check connection pools and network connectivity.",
                    metricContext);
            case MEMORY_LEAK -> String.format(
                    "Potential memory leak detected. %s shows abnormal memory consumption pattern. " +
                            "Application may require restart or code review.",
                    metricContext);
            case TRAFFIC_SPIKE -> String.format(
                    "Traffic spike detected. %s indicates sudden increase in load. " +
                            "Consider scaling resources or enabling rate limiting.",
                    metricContext);
            case DEPENDENCY_FAILURE -> String.format(
                    "Dependency failure suspected. %s suggests upstream or downstream service issues. " +
                            "Check status of dependent services.",
                    metricContext);
            case NETWORK_LATENCY -> String.format(
                    "Network latency issues detected. %s indicates slow response times. " +
                            "Investigate network path and bandwidth.",
                    metricContext);
            default -> String.format(
                    "Anomaly detected: %s. Pattern identified as %s. " +
                            "Review system logs and metrics for more details.",
                    metricContext, patternType);
        };
    }

    /**
     * Calculates similarity score between two anomalies.
     */
    public double calculateSimilarity(Anomaly a1, Anomaly a2) {
        double score = 0.0;

        // Same metric type
        if (a1.getMetricType() == a2.getMetricType()) {
            score += 0.3;
        }

        // Same application
        if (Objects.equals(a1.getApplicationId(), a2.getApplicationId())) {
            score += 0.25;
        }

        // Same detection type
        if (a1.getDetectionType() == a2.getDetectionType()) {
            score += 0.15;
        }

        // Same severity
        if (a1.getSeverity() == a2.getSeverity()) {
            score += 0.15;
        }

        // Similar value (within 20%)
        if (a1.getCurrentValue() != null && a2.getCurrentValue() != null) {
            double diff = Math.abs(a1.getCurrentValue() - a2.getCurrentValue());
            double avg = (a1.getCurrentValue() + a2.getCurrentValue()) / 2;
            if (avg > 0 && diff / avg < 0.2) {
                score += 0.15;
            }
        }

        return score;
    }
}
