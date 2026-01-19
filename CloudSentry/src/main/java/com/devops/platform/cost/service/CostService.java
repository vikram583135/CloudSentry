package com.devops.platform.cost.service;

import com.devops.platform.common.exception.ResourceNotFoundException;
import com.devops.platform.cost.dto.*;
import com.devops.platform.cost.model.*;
import com.devops.platform.cost.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for cost tracking and analytics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CostService {

    private final CloudResourceRepository resourceRepository;
    private final CostRecordRepository costRecordRepository;
    private final CostRecommendationRepository recommendationRepository;

    /**
     * Creates or updates a cloud resource.
     */
    @Transactional
    public CloudResourceResponse createOrUpdateResource(CloudResourceRequest request) {
        CloudResource resource = resourceRepository.findByResourceId(request.getResourceId())
                .orElse(new CloudResource());

        resource.setResourceId(request.getResourceId());
        resource.setName(request.getName());
        resource.setCloudProvider(request.getCloudProvider());
        resource.setResourceType(request.getResourceType());
        resource.setStatus(request.getStatus() != null ? request.getStatus() : ResourceStatus.ACTIVE);
        resource.setRegion(request.getRegion());
        resource.setAvailabilityZone(request.getAvailabilityZone());
        resource.setInstanceType(request.getInstanceType());
        resource.setApplicationId(request.getApplicationId());
        resource.setApplicationName(request.getApplicationName());
        resource.setEnvironment(request.getEnvironment());
        resource.setTeam(request.getTeam());
        resource.setOwner(request.getOwner());
        resource.setHourlyCost(request.getHourlyCost());
        resource.setMonthlyCost(request.getMonthlyCost());
        resource.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        resource.setTags(request.getTags());

        resource = resourceRepository.save(resource);
        log.info("Created/updated resource: id={}, resourceId={}", resource.getId(), resource.getResourceId());

        return toResponse(resource);
    }

    /**
     * Gets a resource by ID.
     */
    public CloudResourceResponse getResource(UUID id) {
        CloudResource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CloudResource", "id", id));
        return toResponse(resource);
    }

    /**
     * Gets all resources with pagination.
     */
    public List<CloudResourceResponse> getAllResources(int limit) {
        return resourceRepository.findAll(PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets resources by provider.
     */
    public List<CloudResourceResponse> getResourcesByProvider(CloudProvider provider, int limit) {
        return resourceRepository.findByCloudProviderOrderByMonthlyCostDesc(provider, PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets idle resources (candidates for termination).
     */
    public List<CloudResourceResponse> getIdleResources() {
        return resourceRepository.findIdleResources(10.0)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets underutilized resources (candidates for rightsizing).
     */
    public List<CloudResourceResponse> getUnderutilizedResources(int limit) {
        return resourceRepository.findUnderutilizedResources(
                30.0, BigDecimal.valueOf(10), PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates resource utilization metrics.
     */
    @Transactional
    public CloudResourceResponse updateUtilization(UUID id, Double cpuUtil, Double memoryUtil, Double diskUtil) {
        CloudResource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CloudResource", "id", id));

        resource.setCpuUtilization(cpuUtil);
        resource.setMemoryUtilization(memoryUtil);
        resource.setDiskUtilization(diskUtil);

        // Check if resource became idle
        if (resource.isIdle() && resource.getIdleSince() == null) {
            resource.setIdleSince(java.time.Instant.now());
            resource.setStatus(ResourceStatus.IDLE);
        } else if (!resource.isIdle() && resource.getIdleSince() != null) {
            resource.setIdleSince(null);
            resource.setStatus(ResourceStatus.ACTIVE);
            resource.setLastActivityAt(java.time.Instant.now());
        }

        resource = resourceRepository.save(resource);
        return toResponse(resource);
    }

    /**
     * Gets cost summary/dashboard data.
     */
    public CostSummary getCostSummary() {
        BigDecimal totalMonthlyCost = resourceRepository.getTotalMonthlyCost();
        if (totalMonthlyCost == null)
            totalMonthlyCost = BigDecimal.ZERO;

        BigDecimal potentialSavings = recommendationRepository.getTotalPotentialSavings();
        if (potentialSavings == null)
            potentialSavings = BigDecimal.ZERO;

        BigDecimal actualSavings = recommendationRepository.getTotalActualSavings();
        if (actualSavings == null)
            actualSavings = BigDecimal.ZERO;

        // Get cost by provider
        Map<String, BigDecimal> costByProvider = new HashMap<>();
        for (Object[] row : resourceRepository.getCostByProvider()) {
            if (row[0] != null && row[1] != null) {
                costByProvider.put(((CloudProvider) row[0]).name(), (BigDecimal) row[1]);
            }
        }

        // Get cost by resource type
        Map<String, BigDecimal> costByResourceType = new HashMap<>();
        for (Object[] row : resourceRepository.getCostByResourceType()) {
            if (row[0] != null && row[1] != null) {
                costByResourceType.put(((ResourceType) row[0]).name(), (BigDecimal) row[1]);
            }
        }

        // Get cost by team
        Map<String, BigDecimal> costByTeam = new HashMap<>();
        for (Object[] row : resourceRepository.getCostByTeam()) {
            if (row[0] != null && row[1] != null) {
                costByTeam.put((String) row[0], (BigDecimal) row[1]);
            }
        }

        // Get cost by environment
        Map<String, BigDecimal> costByEnvironment = new HashMap<>();
        for (Object[] row : resourceRepository.getCostByEnvironment()) {
            if (row[0] != null && row[1] != null) {
                costByEnvironment.put((String) row[0], (BigDecimal) row[1]);
            }
        }

        // Get cost trend (last 30 days)
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        List<CostSummary.CostTrendPoint> trend = new ArrayList<>();
        for (Object[] row : costRecordRepository.getDailyCosts(startDate, endDate)) {
            if (row[0] != null && row[1] != null) {
                trend.add(CostSummary.CostTrendPoint.builder()
                        .date(row[0].toString())
                        .cost((BigDecimal) row[1])
                        .build());
            }
        }

        return CostSummary.builder()
                .totalMonthlyCost(totalMonthlyCost)
                .totalDailyCost(totalMonthlyCost.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP))
                .totalPotentialSavings(potentialSavings)
                .totalActualSavings(actualSavings)
                .currency("USD")
                .totalResources(resourceRepository.count())
                .idleResources(resourceRepository.countByStatus(ResourceStatus.IDLE))
                .underutilizedResources(resourceRepository.countByStatus(ResourceStatus.UNDERUTILIZED))
                .openRecommendations(recommendationRepository.countByStatus("OPEN"))
                .implementedRecommendations(recommendationRepository.countByStatus("IMPLEMENTED"))
                .costByProvider(costByProvider)
                .costByResourceType(costByResourceType)
                .costByTeam(costByTeam)
                .costByEnvironment(costByEnvironment)
                .costTrend(trend)
                .build();
    }

    private CloudResourceResponse toResponse(CloudResource resource) {
        return CloudResourceResponse.builder()
                .id(resource.getId())
                .resourceId(resource.getResourceId())
                .name(resource.getName())
                .cloudProvider(resource.getCloudProvider())
                .resourceType(resource.getResourceType())
                .status(resource.getStatus())
                .region(resource.getRegion())
                .instanceType(resource.getInstanceType())
                .applicationId(resource.getApplicationId())
                .applicationName(resource.getApplicationName())
                .environment(resource.getEnvironment())
                .team(resource.getTeam())
                .owner(resource.getOwner())
                .hourlyCost(resource.getHourlyCost())
                .monthlyCost(resource.getMonthlyCost())
                .currency(resource.getCurrency())
                .cpuUtilization(resource.getCpuUtilization())
                .memoryUtilization(resource.getMemoryUtilization())
                .diskUtilization(resource.getDiskUtilization())
                .isIdle(resource.isIdle())
                .idleSince(resource.getIdleSince())
                .potentialSavings(resource.getPotentialMonthlySavings())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .build();
    }
}
