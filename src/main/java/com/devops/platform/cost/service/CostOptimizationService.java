package com.devops.platform.cost.service;

import com.devops.platform.common.exception.ResourceNotFoundException;
import com.devops.platform.cost.dto.CostRecommendationResponse;
import com.devops.platform.cost.model.*;
import com.devops.platform.cost.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating cost optimization recommendations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CostOptimizationService {

    private final CloudResourceRepository resourceRepository;
    private final CostRecommendationRepository recommendationRepository;

    /**
     * Generates optimization recommendations for all resources.
     */
    @Transactional
    public List<CostRecommendationResponse> generateRecommendations() {
        List<CostRecommendation> newRecommendations = new ArrayList<>();

        // 1. Find idle resources -> TERMINATE_IDLE
        List<CloudResource> idleResources = resourceRepository.findIdleResources(10.0);
        for (CloudResource resource : idleResources) {
            if (!hasOpenRecommendation(resource.getId(), OptimizationType.TERMINATE_IDLE)) {
                CostRecommendation rec = createTerminateIdleRecommendation(resource);
                newRecommendations.add(rec);
            }
        }

        // 2. Find underutilized resources -> RIGHTSIZE
        List<CloudResource> underutilized = resourceRepository.findUnderutilizedResources(
                30.0, BigDecimal.valueOf(50), PageRequest.of(0, 100));
        for (CloudResource resource : underutilized) {
            if (!resource.isIdle() && !hasOpenRecommendation(resource.getId(), OptimizationType.RIGHTSIZE)) {
                CostRecommendation rec = createRightsizeRecommendation(resource);
                newRecommendations.add(rec);
            }
        }

        List<CostRecommendation> saved = recommendationRepository.saveAll(newRecommendations);
        log.info("Generated {} new recommendations", saved.size());

        return saved.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Gets all open recommendations.
     */
    public List<CostRecommendationResponse> getOpenRecommendations(int limit) {
        return recommendationRepository.findByStatusOrderByEstimatedSavingsDesc("OPEN", PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets recommendations by type.
     */
    public List<CostRecommendationResponse> getRecommendationsByType(OptimizationType type, int limit) {
        return recommendationRepository.findByOptimizationTypeAndStatusOrderByEstimatedSavingsDesc(
                type, "OPEN", PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets a recommendation by ID.
     */
    public CostRecommendationResponse getRecommendation(UUID id) {
        CostRecommendation rec = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CostRecommendation", "id", id));
        return toResponse(rec);
    }

    /**
     * Accepts a recommendation.
     */
    @Transactional
    public CostRecommendationResponse acceptRecommendation(UUID id) {
        CostRecommendation rec = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CostRecommendation", "id", id));

        rec.setStatus("ACCEPTED");
        rec.setAcceptedAt(Instant.now());
        rec = recommendationRepository.save(rec);

        log.info("Accepted recommendation: id={}", id);
        return toResponse(rec);
    }

    /**
     * Marks a recommendation as implemented.
     */
    @Transactional
    public CostRecommendationResponse implementRecommendation(UUID id, BigDecimal actualSavings) {
        CostRecommendation rec = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CostRecommendation", "id", id));

        rec.setStatus("IMPLEMENTED");
        rec.setImplementedAt(Instant.now());
        rec.setActualSavings(actualSavings != null ? actualSavings : rec.getEstimatedSavings());
        rec = recommendationRepository.save(rec);

        log.info("Implemented recommendation: id={}, actualSavings={}", id, rec.getActualSavings());
        return toResponse(rec);
    }

    /**
     * Rejects a recommendation.
     */
    @Transactional
    public CostRecommendationResponse rejectRecommendation(UUID id, String reason) {
        CostRecommendation rec = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CostRecommendation", "id", id));

        rec.setStatus("REJECTED");
        rec.setRejectionReason(reason);
        rec = recommendationRepository.save(rec);

        log.info("Rejected recommendation: id={}, reason={}", id, reason);
        return toResponse(rec);
    }

    /**
     * Dismisses a recommendation.
     */
    @Transactional
    public CostRecommendationResponse dismissRecommendation(UUID id) {
        CostRecommendation rec = recommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CostRecommendation", "id", id));

        rec.setStatus("DISMISSED");
        rec = recommendationRepository.save(rec);

        log.info("Dismissed recommendation: id={}", id);
        return toResponse(rec);
    }

    /**
     * Gets optimization summary.
     */
    public Map<String, Object> getOptimizationSummary() {
        BigDecimal potentialSavings = recommendationRepository.getTotalPotentialSavings();
        BigDecimal actualSavings = recommendationRepository.getTotalActualSavings();

        List<Object[]> byType = recommendationRepository.getRecommendationsByType();
        List<Map<String, Object>> typeBreakdown = new ArrayList<>();
        for (Object[] row : byType) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", row[0].toString());
            item.put("count", row[1]);
            item.put("potentialSavings", row[2]);
            typeBreakdown.add(item);
        }

        return Map.of(
                "totalPotentialSavings", potentialSavings != null ? potentialSavings : BigDecimal.ZERO,
                "totalActualSavings", actualSavings != null ? actualSavings : BigDecimal.ZERO,
                "openRecommendations", recommendationRepository.countByStatus("OPEN"),
                "implementedRecommendations", recommendationRepository.countByStatus("IMPLEMENTED"),
                "byType", typeBreakdown);
    }

    private boolean hasOpenRecommendation(UUID resourceId, OptimizationType type) {
        List<CostRecommendation> existing = recommendationRepository.findByResourceIdOrderByCreatedAtDesc(resourceId);
        return existing.stream()
                .anyMatch(r -> r.getOptimizationType() == type && "OPEN".equals(r.getStatus()));
    }

    private CostRecommendation createTerminateIdleRecommendation(CloudResource resource) {
        BigDecimal savings = resource.getMonthlyCost() != null ? resource.getMonthlyCost() : BigDecimal.ZERO;

        return CostRecommendation.builder()
                .resourceId(resource.getId())
                .applicationId(resource.getApplicationId())
                .optimizationType(OptimizationType.TERMINATE_IDLE)
                .title("Terminate Idle " + resource.getResourceType() + ": " + resource.getName())
                .description(String.format("Resource %s has been idle (CPU < 10%%, Memory < 10%%) since %s. " +
                        "Consider terminating to save costs.", resource.getName(), resource.getIdleSince()))
                .currentCost(savings)
                .projectedCost(BigDecimal.ZERO)
                .estimatedSavings(savings)
                .savingsPercentage(100.0)
                .effortLevel("LOW")
                .riskLevel("LOW")
                .implementationSteps(
                        "1. Verify no critical workloads\n2. Take snapshot/backup if needed\n3. Terminate resource\n4. Verify no impact")
                .status("OPEN")
                .build();
    }

    private CostRecommendation createRightsizeRecommendation(CloudResource resource) {
        BigDecimal currentCost = resource.getMonthlyCost() != null ? resource.getMonthlyCost() : BigDecimal.ZERO;
        BigDecimal savings = currentCost.multiply(BigDecimal.valueOf(0.3)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal projectedCost = currentCost.subtract(savings);

        return CostRecommendation.builder()
                .resourceId(resource.getId())
                .applicationId(resource.getApplicationId())
                .optimizationType(OptimizationType.RIGHTSIZE)
                .title("Rightsize " + resource.getResourceType() + ": " + resource.getName())
                .description(String.format("Resource %s is underutilized (CPU: %.1f%%, Memory: %.1f%%). " +
                        "Consider downsizing to a smaller instance type.",
                        resource.getName(), resource.getCpuUtilization(), resource.getMemoryUtilization()))
                .currentCost(currentCost)
                .projectedCost(projectedCost)
                .estimatedSavings(savings)
                .savingsPercentage(30.0)
                .effortLevel("MEDIUM")
                .riskLevel("LOW")
                .implementationSteps(
                        "1. Review current workload requirements\n2. Test smaller instance in staging\n3. Schedule maintenance window\n4. Resize instance\n5. Monitor performance")
                .status("OPEN")
                .build();
    }

    private CostRecommendationResponse toResponse(CostRecommendation rec) {
        List<String> steps = rec.getImplementationSteps() != null
                ? Arrays.asList(rec.getImplementationSteps().split("\n"))
                : Collections.emptyList();

        return CostRecommendationResponse.builder()
                .id(rec.getId())
                .resourceId(rec.getResourceId())
                .applicationId(rec.getApplicationId())
                .optimizationType(rec.getOptimizationType())
                .title(rec.getTitle())
                .description(rec.getDescription())
                .currentCost(rec.getCurrentCost())
                .projectedCost(rec.getProjectedCost())
                .estimatedSavings(rec.getEstimatedSavings())
                .savingsPercentage(rec.getSavingsPercentage())
                .currency(rec.getCurrency())
                .effortLevel(rec.getEffortLevel())
                .riskLevel(rec.getRiskLevel())
                .implementationSteps(steps)
                .status(rec.getStatus())
                .createdAt(rec.getCreatedAt())
                .build();
    }
}
