package com.devops.platform.incident.repository;

import com.devops.platform.incident.model.IncidentTimeline;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for IncidentTimeline entities.
 */
@Repository
public interface IncidentTimelineRepository extends JpaRepository<IncidentTimeline, UUID> {

    List<IncidentTimeline> findByIncidentIdOrderByCreatedAtDesc(UUID incidentId);

    List<IncidentTimeline> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);

    List<IncidentTimeline> findByIncidentIdOrderByCreatedAtDesc(UUID incidentId, Pageable pageable);

    List<IncidentTimeline> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
