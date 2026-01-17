package com.devops.platform.rca.repository;

import com.devops.platform.rca.model.ConfidenceLevel;
import com.devops.platform.rca.model.PatternType;
import com.devops.platform.rca.model.RcaResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for RcaResult entities.
 */
@Repository
public interface RcaResultRepository extends JpaRepository<RcaResult, UUID> {

    List<RcaResult> findByIncidentIdOrderByCreatedAtDesc(UUID incidentId);

    List<RcaResult> findByAnomalyIdOrderByCreatedAtDesc(UUID anomalyId);

    List<RcaResult> findByApplicationIdOrderByCreatedAtDesc(UUID applicationId, Pageable pageable);

    List<RcaResult> findByPatternTypeOrderByCreatedAtDesc(PatternType patternType, Pageable pageable);

    @Query("SELECT r FROM RcaResult r WHERE r.applicationId = :appId AND r.patternType = :pattern ORDER BY r.createdAt DESC")
    List<RcaResult> findSimilarResults(
            @Param("appId") UUID applicationId,
            @Param("pattern") PatternType patternType,
            Pageable pageable);

    @Query("SELECT r FROM RcaResult r WHERE r.wasHelpful = true AND r.patternType = :pattern ORDER BY r.createdAt DESC")
    List<RcaResult> findHelpfulResultsByPattern(@Param("pattern") PatternType patternType, Pageable pageable);

    @Query("SELECT r.patternType, COUNT(r) FROM RcaResult r WHERE r.createdAt > :since GROUP BY r.patternType ORDER BY COUNT(r) DESC")
    List<Object[]> countByPatternSince(@Param("since") Instant since);

    @Query("SELECT COUNT(r) FROM RcaResult r WHERE r.wasHelpful = true")
    long countHelpful();

    @Query("SELECT COUNT(r) FROM RcaResult r WHERE r.wasHelpful = false")
    long countNotHelpful();
}
