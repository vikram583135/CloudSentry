package com.devops.platform.cost.repository;

import com.devops.platform.cost.model.CloudProvider;
import com.devops.platform.cost.model.CloudResource;
import com.devops.platform.cost.model.ResourceStatus;
import com.devops.platform.cost.model.ResourceType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CloudResource entities.
 */
@Repository
public interface CloudResourceRepository extends JpaRepository<CloudResource, UUID> {

    Optional<CloudResource> findByResourceId(String resourceId);

    List<CloudResource> findByCloudProviderOrderByMonthlyCostDesc(CloudProvider provider, Pageable pageable);

    List<CloudResource> findByResourceTypeOrderByMonthlyCostDesc(ResourceType type, Pageable pageable);

    List<CloudResource> findByStatusOrderByMonthlyCostDesc(ResourceStatus status, Pageable pageable);

    List<CloudResource> findByApplicationIdOrderByMonthlyCostDesc(UUID applicationId);

    @Query("SELECT r FROM CloudResource r WHERE r.status = 'IDLE' OR (r.cpuUtilization < :threshold AND r.memoryUtilization < :threshold)")
    List<CloudResource> findIdleResources(@Param("threshold") Double threshold);

    @Query("SELECT r FROM CloudResource r WHERE r.cpuUtilization < :threshold AND r.memoryUtilization < :threshold AND r.monthlyCost > :minCost ORDER BY r.monthlyCost DESC")
    List<CloudResource> findUnderutilizedResources(
            @Param("threshold") Double threshold,
            @Param("minCost") BigDecimal minCost,
            Pageable pageable);

    @Query("SELECT SUM(r.monthlyCost) FROM CloudResource r WHERE r.status != 'TERMINATED'")
    BigDecimal getTotalMonthlyCost();

    @Query("SELECT SUM(r.monthlyCost) FROM CloudResource r WHERE r.applicationId = :appId AND r.status != 'TERMINATED'")
    BigDecimal getTotalMonthlyCostByApplication(@Param("appId") UUID applicationId);

    @Query("SELECT r.cloudProvider, SUM(r.monthlyCost) FROM CloudResource r WHERE r.status != 'TERMINATED' GROUP BY r.cloudProvider")
    List<Object[]> getCostByProvider();

    @Query("SELECT r.resourceType, SUM(r.monthlyCost) FROM CloudResource r WHERE r.status != 'TERMINATED' GROUP BY r.resourceType ORDER BY SUM(r.monthlyCost) DESC")
    List<Object[]> getCostByResourceType();

    @Query("SELECT r.team, SUM(r.monthlyCost) FROM CloudResource r WHERE r.status != 'TERMINATED' AND r.team IS NOT NULL GROUP BY r.team ORDER BY SUM(r.monthlyCost) DESC")
    List<Object[]> getCostByTeam();

    @Query("SELECT r.environment, SUM(r.monthlyCost) FROM CloudResource r WHERE r.status != 'TERMINATED' AND r.environment IS NOT NULL GROUP BY r.environment")
    List<Object[]> getCostByEnvironment();

    long countByStatus(ResourceStatus status);
}
