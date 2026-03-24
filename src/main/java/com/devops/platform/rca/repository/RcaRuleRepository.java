package com.devops.platform.rca.repository;

import com.devops.platform.metrics.model.MetricType;
import com.devops.platform.rca.model.PatternType;
import com.devops.platform.rca.model.RcaRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for RcaRule entities.
 */
@Repository
public interface RcaRuleRepository extends JpaRepository<RcaRule, UUID> {

    List<RcaRule> findByEnabledTrueOrderByPriorityAsc();

    List<RcaRule> findByPatternTypeAndEnabledTrue(PatternType patternType);

    List<RcaRule> findByMetricTypeAndEnabledTrue(MetricType metricType);

    @Query("SELECT r FROM RcaRule r WHERE r.enabled = true AND (r.metricType = :metricType OR r.metricType IS NULL) ORDER BY r.priority ASC")
    List<RcaRule> findApplicableRules(@Param("metricType") MetricType metricType);

    List<RcaRule> findTop10ByEnabledTrueOrderByTimesMatchedDesc();

    boolean existsByName(String name);
}
