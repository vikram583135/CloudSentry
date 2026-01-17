package com.devops.platform.rca.service;

import com.devops.platform.analyzer.model.Anomaly;
import com.devops.platform.analyzer.repository.AnomalyRepository;
import com.devops.platform.common.exception.BadRequestException;
import com.devops.platform.common.exception.ResourceNotFoundException;
import com.devops.platform.incident.model.Incident;
import com.devops.platform.incident.repository.IncidentRepository;
import com.devops.platform.rca.dto.RcaRequest;
import com.devops.platform.rca.dto.RcaResponse;
import com.devops.platform.rca.dto.RcaRuleRequest;
import com.devops.platform.rca.engine.PatternMatcher;
import com.devops.platform.rca.engine.RuleEngine;
import com.devops.platform.rca.model.*;
import com.devops.platform.rca.repository.RcaResultRepository;
import com.devops.platform.rca.repository.RcaRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for root cause analysis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RcaService {

    private final RcaRuleRepository ruleRepository;
    private final RcaResultRepository resultRepository;
    private final AnomalyRepository anomalyRepository;
    private final IncidentRepository incidentRepository;
    private final RuleEngine ruleEngine;
    private final PatternMatcher patternMatcher;

    /**
     * Analyzes an anomaly for root cause.
     */
    @Transactional
    public RcaResponse analyzeAnomaly(UUID anomalyId) {
        Anomaly anomaly = anomalyRepository.findById(anomalyId)
                .orElseThrow(() -> new ResourceNotFoundException("Anomaly", "id", anomalyId));

        return performAnalysis(anomaly, null);
    }

    /**
     * Analyzes an incident for root cause.
     */
    @Transactional
    public RcaResponse analyzeIncident(UUID incidentId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", incidentId));

        // If incident was triggered by anomaly, analyze that
        if (incident.getTriggeredByAnomalyId() != null) {
            return analyzeAnomaly(incident.getTriggeredByAnomalyId());
        }

        // Otherwise, create a synthetic analysis
        return createIncidentAnalysis(incident);
    }

    /**
     * Performs RCA based on request parameters.
     */
    @Transactional
    public RcaResponse analyze(RcaRequest request) {
        if (request.getAnomalyId() != null) {
            return analyzeAnomaly(request.getAnomalyId());
        } else if (request.getIncidentId() != null) {
            return analyzeIncident(request.getIncidentId());
        } else {
            throw new BadRequestException("Either anomalyId or incidentId is required");
        }
    }

    /**
     * Performs the core analysis logic.
     */
    private RcaResponse performAnalysis(Anomaly anomaly, Incident incident) {
        // Evaluate rules
        List<RuleEngine.RuleMatch> matches = ruleEngine.evaluateAnomaly(anomaly);

        PatternType patternType = ruleEngine.detectPatternFromAnomaly(anomaly);
        List<String> suggestedActions = patternMatcher.getSuggestedActions(patternType);
        String summary = patternMatcher.generateRootCauseSummary(anomaly, patternType);

        RcaRule matchedRule = null;
        ConfidenceLevel confidence = ConfidenceLevel.MEDIUM;
        double confidenceScore = 0.5;
        String detailedAnalysis = summary;

        if (!matches.isEmpty()) {
            RuleEngine.RuleMatch bestMatch = matches.get(0);
            matchedRule = bestMatch.getRule();
            confidence = bestMatch.getConfidenceLevel();
            confidenceScore = bestMatch.getConfidenceScore();

            if (matchedRule.getRootCauseTemplate() != null) {
                detailedAnalysis = matchedRule.getRootCauseTemplate()
                        .replace("{metric}", anomaly.getMetricType().toString())
                        .replace("{value}", String.format("%.2f", anomaly.getCurrentValue()))
                        .replace("{app}",
                                anomaly.getApplicationName() != null ? anomaly.getApplicationName() : "Unknown");
            }

            if (matchedRule.getSuggestedActions() != null) {
                suggestedActions = Arrays.asList(matchedRule.getSuggestedActions().split("\n"));
            }
        }

        // Find similar past incidents
        List<UUID> similarIncidents = findSimilarIncidents(anomaly, patternType);

        // Save result
        RcaResult result = RcaResult.builder()
                .incidentId(incident != null ? incident.getId() : null)
                .anomalyId(anomaly.getId())
                .applicationId(anomaly.getApplicationId())
                .patternType(patternType)
                .confidenceLevel(confidence)
                .confidenceScore(confidenceScore)
                .rootCauseSummary(summary)
                .detailedAnalysis(detailedAnalysis)
                .suggestedActions(String.join("\n", suggestedActions))
                .matchedRuleId(matchedRule != null ? matchedRule.getId() : null)
                .matchedRuleName(matchedRule != null ? matchedRule.getName() : null)
                .similarIncidents(similarIncidents.stream().map(UUID::toString).collect(Collectors.joining(",")))
                .isAiGenerated(false)
                .build();

        result = resultRepository.save(result);
        log.info("RCA completed: id={}, pattern={}, confidence={}",
                result.getId(), patternType, confidence);

        return toResponse(result, suggestedActions, similarIncidents);
    }

    /**
     * Creates analysis for incidents without anomaly trigger.
     */
    private RcaResponse createIncidentAnalysis(Incident incident) {
        PatternType patternType = PatternType.CUSTOM;
        ConfidenceLevel confidence = ConfidenceLevel.LOW;

        String summary = String.format("Manual incident analysis for %s: %s",
                incident.getIncidentNumber(), incident.getTitle());

        List<String> suggestedActions = List.of(
                "Review incident description and timeline",
                "Check application logs around incident time",
                "Compare with similar past incidents",
                "Engage relevant teams for investigation",
                "Document findings for post-mortem");

        RcaResult result = RcaResult.builder()
                .incidentId(incident.getId())
                .applicationId(incident.getApplicationId())
                .patternType(patternType)
                .confidenceLevel(confidence)
                .confidenceScore(0.3)
                .rootCauseSummary(summary)
                .detailedAnalysis("Incident requires manual investigation. No triggering anomaly found.")
                .suggestedActions(String.join("\n", suggestedActions))
                .isAiGenerated(false)
                .build();

        result = resultRepository.save(result);
        return toResponse(result, suggestedActions, Collections.emptyList());
    }

    /**
     * Finds similar past incidents.
     */
    private List<UUID> findSimilarIncidents(Anomaly anomaly, PatternType patternType) {
        if (anomaly.getApplicationId() == null) {
            return Collections.emptyList();
        }

        List<RcaResult> similar = resultRepository.findSimilarResults(
                anomaly.getApplicationId(), patternType, PageRequest.of(0, 5));

        return similar.stream()
                .filter(r -> r.getIncidentId() != null)
                .map(RcaResult::getIncidentId)
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * Gets RCA results for an incident.
     */
    public List<RcaResponse> getResultsForIncident(UUID incidentId) {
        return resultRepository.findByIncidentIdOrderByCreatedAtDesc(incidentId)
                .stream()
                .map(this::toSimpleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets RCA result by ID.
     */
    public RcaResponse getResult(UUID id) {
        RcaResult result = resultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RcaResult", "id", id));
        return toSimpleResponse(result);
    }

    /**
     * Provides feedback on RCA result.
     */
    @Transactional
    public void provideFeedback(UUID resultId, boolean wasHelpful, String feedback) {
        RcaResult result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("RcaResult", "id", resultId));

        result.setWasHelpful(wasHelpful);
        result.setUserFeedback(feedback);
        resultRepository.save(result);

        log.info("RCA feedback: id={}, helpful={}", resultId, wasHelpful);
    }

    /**
     * Gets RCA statistics.
     */
    public Map<String, Object> getStats() {
        long helpful = resultRepository.countHelpful();
        long notHelpful = resultRepository.countNotHelpful();
        long total = resultRepository.count();

        return Map.of(
                "totalAnalyses", total,
                "helpfulCount", helpful,
                "notHelpfulCount", notHelpful,
                "helpfulPercentage", total > 0 ? (helpful * 100.0 / (helpful + notHelpful)) : 0);
    }

    // Rule management methods

    @Transactional
    public RcaRule createRule(RcaRuleRequest request) {
        if (ruleRepository.existsByName(request.getName())) {
            throw new BadRequestException("Rule with name '" + request.getName() + "' already exists");
        }

        RcaRule rule = RcaRule.builder()
                .name(request.getName())
                .description(request.getDescription())
                .patternType(request.getPatternType())
                .metricType(request.getMetricType())
                .conditionExpression(request.getConditionExpression())
                .thresholdValue(request.getThresholdValue())
                .thresholdOperator(request.getThresholdOperator())
                .timeWindowMinutes(request.getTimeWindowMinutes())
                .rootCauseTemplate(request.getRootCauseTemplate())
                .suggestedActions(
                        request.getSuggestedActions() != null ? String.join("\n", request.getSuggestedActions()) : null)
                .runbookUrl(request.getRunbookUrl())
                .priority(request.getPriority())
                .enabled(request.getEnabled())
                .build();

        rule = ruleRepository.save(rule);
        log.info("Created RCA rule: id={}, name={}", rule.getId(), rule.getName());
        return rule;
    }

    public List<RcaRule> getAllRules() {
        return ruleRepository.findByEnabledTrueOrderByPriorityAsc();
    }

    public RcaRule getRule(UUID id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RcaRule", "id", id));
    }

    @Transactional
    public void deleteRule(UUID id) {
        if (!ruleRepository.existsById(id)) {
            throw new ResourceNotFoundException("RcaRule", "id", id);
        }
        ruleRepository.deleteById(id);
        log.info("Deleted RCA rule: id={}", id);
    }

    private RcaResponse toResponse(RcaResult result, List<String> actions, List<UUID> similarIncidents) {
        return RcaResponse.builder()
                .id(result.getId())
                .incidentId(result.getIncidentId())
                .anomalyId(result.getAnomalyId())
                .applicationId(result.getApplicationId())
                .patternType(result.getPatternType())
                .confidenceLevel(result.getConfidenceLevel())
                .confidenceScore(result.getConfidenceScore())
                .rootCauseSummary(result.getRootCauseSummary())
                .detailedAnalysis(result.getDetailedAnalysis())
                .suggestedActions(actions)
                .matchedRuleId(result.getMatchedRuleId())
                .matchedRuleName(result.getMatchedRuleName())
                .similarIncidents(similarIncidents)
                .isAiGenerated(result.getIsAiGenerated())
                .aiModelUsed(result.getAiModelUsed())
                .createdAt(result.getCreatedAt())
                .build();
    }

    private RcaResponse toSimpleResponse(RcaResult result) {
        List<String> actions = result.getSuggestedActions() != null
                ? Arrays.asList(result.getSuggestedActions().split("\n"))
                : Collections.emptyList();
        List<UUID> similar = result.getSimilarIncidents() != null && !result.getSimilarIncidents().isEmpty()
                ? Arrays.stream(result.getSimilarIncidents().split(","))
                        .map(UUID::fromString)
                        .collect(Collectors.toList())
                : Collections.emptyList();
        return toResponse(result, actions, similar);
    }
}
