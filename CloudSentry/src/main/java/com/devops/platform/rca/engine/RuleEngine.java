package com.devops.platform.rca.engine;

import com.devops.platform.analyzer.model.Anomaly;
import com.devops.platform.metrics.model.MetricType;
import com.devops.platform.rca.model.ConfidenceLevel;
import com.devops.platform.rca.model.PatternType;
import com.devops.platform.rca.model.RcaRule;
import com.devops.platform.rca.repository.RcaRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Rule engine for pattern-based root cause analysis.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RuleEngine {

    private final RcaRuleRepository ruleRepository;

    /**
     * Evaluates an anomaly against all applicable rules.
     */
    public List<RuleMatch> evaluateAnomaly(Anomaly anomaly) {
        List<RcaRule> applicableRules = ruleRepository.findApplicableRules(anomaly.getMetricType());
        List<RuleMatch> matches = new ArrayList<>();

        for (RcaRule rule : applicableRules) {
            Optional<RuleMatch> match = evaluateRule(anomaly, rule);
            match.ifPresent(matches::add);
        }

        // Sort by confidence score descending
        matches.sort((a, b) -> Double.compare(b.getConfidenceScore(), a.getConfidenceScore()));
        return matches;
    }

    /**
     * Evaluates a single rule against an anomaly.
     */
    private Optional<RuleMatch> evaluateRule(Anomaly anomaly, RcaRule rule) {
        boolean matches = false;
        double confidenceScore = 0.0;
        String matchReason = null;

        // Check pattern type compatibility
        PatternType detectedPattern = detectPatternFromAnomaly(anomaly);
        if (rule.getPatternType() == detectedPattern) {
            matches = true;
            confidenceScore += 0.4;
            matchReason = "Pattern type matches: " + detectedPattern;
        }

        // Check threshold conditions
        if (rule.getThresholdValue() != null && rule.getThresholdOperator() != null) {
            boolean thresholdMatch = evaluateThreshold(
                    anomaly.getCurrentValue(),
                    rule.getThresholdValue(),
                    rule.getThresholdOperator());
            if (thresholdMatch) {
                matches = true;
                confidenceScore += 0.3;
                matchReason = (matchReason != null ? matchReason + "; " : "") +
                        "Threshold condition met";
            }
        }

        // Check metric type match
        if (rule.getMetricType() == anomaly.getMetricType()) {
            confidenceScore += 0.2;
        }

        // Boost for severity alignment
        if (anomaly.getSeverity() != null) {
            confidenceScore += switch (anomaly.getSeverity()) {
                case CRITICAL -> 0.1;
                case HIGH -> 0.05;
                default -> 0.0;
            };
        }

        if (matches && confidenceScore > 0.3) {
            rule.incrementMatchCount();

            return Optional.of(RuleMatch.builder()
                    .rule(rule)
                    .confidenceScore(Math.min(confidenceScore, 1.0))
                    .confidenceLevel(calculateConfidenceLevel(confidenceScore))
                    .matchReason(matchReason)
                    .detectedPattern(detectedPattern)
                    .build());
        }

        return Optional.empty();
    }

    /**
     * Detects pattern type from anomaly characteristics.
     */
    public PatternType detectPatternFromAnomaly(Anomaly anomaly) {
        MetricType metricType = anomaly.getMetricType();

        return switch (metricType) {
            case CPU_USAGE -> {
                if (anomaly.getCurrentValue() > 95)
                    yield PatternType.RESOURCE_EXHAUSTION;
                yield PatternType.TRAFFIC_SPIKE;
            }
            case MEMORY_USAGE -> {
                if (anomaly.getDeviationPercentage() != null && anomaly.getDeviationPercentage() > 50) {
                    yield PatternType.MEMORY_LEAK;
                }
                yield PatternType.RESOURCE_EXHAUSTION;
            }
            case DISK_USAGE -> PatternType.RESOURCE_EXHAUSTION;
            case RESPONSE_TIME, LATENCY_P95, LATENCY_P99 -> PatternType.NETWORK_LATENCY;
            case ERROR_COUNT -> PatternType.DEPENDENCY_FAILURE;
            case ACTIVE_CONNECTIONS -> PatternType.CONNECTION_ISSUE;
            case THROUGHPUT, REQUEST_COUNT -> PatternType.TRAFFIC_SPIKE;
            case QUEUE_SIZE -> PatternType.DISK_IO_BOTTLENECK;
            default -> PatternType.CUSTOM;
        };
    }

    private boolean evaluateThreshold(Double value, Double threshold, String operator) {
        if (value == null || threshold == null)
            return false;

        return switch (operator.toUpperCase()) {
            case "GT", ">" -> value > threshold;
            case "GTE", ">=" -> value >= threshold;
            case "LT", "<" -> value < threshold;
            case "LTE", "<=" -> value <= threshold;
            case "EQ", "=" -> Math.abs(value - threshold) < 0.001;
            default -> false;
        };
    }

    private ConfidenceLevel calculateConfidenceLevel(double score) {
        if (score >= 0.8)
            return ConfidenceLevel.HIGH;
        if (score >= 0.5)
            return ConfidenceLevel.MEDIUM;
        if (score >= 0.3)
            return ConfidenceLevel.LOW;
        return ConfidenceLevel.SPECULATIVE;
    }

    /**
     * Match result from rule evaluation.
     */
    @lombok.Data
    @lombok.Builder
    public static class RuleMatch {
        private RcaRule rule;
        private double confidenceScore;
        private ConfidenceLevel confidenceLevel;
        private String matchReason;
        private PatternType detectedPattern;
    }
}
