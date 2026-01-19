package com.devops.platform.cost.repository;

import com.devops.platform.cost.model.CostRecommendation;
import com.devops.platform.cost.model.OptimizationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Repository for CostRecommendation entities.
 */
@Repository
public interface CostRecommendationRepository extends JpaRepository<CostRecommendation, UUID> {

    List<CostRecommendation> findByStatusOrderByEstimatedSavingsDesc(String status, Pageable pageable);

    List<CostRecommendation> findByResourceIdOrderByCreatedAtDesc(UUID resourceId);

    List<CostRecommendation> findByApplicationIdOrderByEstimatedSavingsDesc(UUID applicationId);

    List<CostRecommendation> findByOptimizationTypeAndStatusOrderByEstimatedSavingsDesc(
            OptimizationType type, String status, Pageable pageable);

    @Query("SELECT SUM(r.estimatedSavings) FROM CostRecommendation r WHERE r.status = 'OPEN'")
    BigDecimal getTotalPotentialSavings();

    @Query("SELECT SUM(r.actualSavings) FROM CostRecommendation r WHERE r.status = 'IMPLEMENTED' AND r.actualSavings IS NOT NULL")
    BigDecimal getTotalActualSavings();

    @Query("SELECT r.optimizationType, COUNT(r), SUM(r.estimatedSavings) FROM CostRecommendation r WHERE r.status = 'OPEN' GROUP BY r.optimizationType ORDER BY SUM(r.estimatedSavings) DESC")
    List<Object[]> getRecommendationsByType();

    long countByStatus(String status);
}
