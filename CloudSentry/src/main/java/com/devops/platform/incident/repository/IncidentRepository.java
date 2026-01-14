package com.devops.platform.incident.repository;

import com.devops.platform.incident.model.Incident;
import com.devops.platform.incident.model.IncidentPriority;
import com.devops.platform.incident.model.IncidentSeverity;
import com.devops.platform.incident.model.IncidentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Incident entities.
 */
@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID> {

    Optional<Incident> findByIncidentNumber(String incidentNumber);

    List<Incident> findByStatusOrderByCreatedAtDesc(IncidentStatus status, Pageable pageable);

    List<Incident> findByStatusInOrderByCreatedAtDesc(List<IncidentStatus> statuses, Pageable pageable);

    List<Incident> findBySeverityAndStatusOrderByCreatedAtDesc(
            IncidentSeverity severity, IncidentStatus status, Pageable pageable);

    List<Incident> findByApplicationIdOrderByCreatedAtDesc(UUID applicationId, Pageable pageable);

    List<Incident> findByApplicationIdAndStatusOrderByCreatedAtDesc(
            UUID applicationId, IncidentStatus status, Pageable pageable);

    List<Incident> findByAssignedToOrderByCreatedAtDesc(UUID assignedTo, Pageable pageable);

    List<Incident> findByAssignedTeamOrderByCreatedAtDesc(String team, Pageable pageable);

    @Query("SELECT i FROM Incident i WHERE i.createdAt BETWEEN :start AND :end ORDER BY i.createdAt DESC")
    List<Incident> findByTimeRange(
            @Param("start") Instant startTime,
            @Param("end") Instant endTime,
            Pageable pageable);

    @Query("SELECT i FROM Incident i WHERE i.applicationId = :appId AND i.status IN :statuses AND i.createdAt > :since")
    List<Incident> findRecentByApplicationAndStatus(
            @Param("appId") UUID applicationId,
            @Param("statuses") List<IncidentStatus> statuses,
            @Param("since") Instant since);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.status IN :statuses")
    long countByStatusIn(@Param("statuses") List<IncidentStatus> statuses);

    @Query("SELECT i.severity, COUNT(i) FROM Incident i WHERE i.status IN :statuses GROUP BY i.severity")
    List<Object[]> countBySeverityAndStatusIn(@Param("statuses") List<IncidentStatus> statuses);

    @Query("SELECT i.status, COUNT(i) FROM Incident i GROUP BY i.status")
    List<Object[]> countByStatus();

    @Query("SELECT AVG(i.timeToAcknowledgeMinutes) FROM Incident i WHERE i.timeToAcknowledgeMinutes IS NOT NULL AND i.createdAt > :since")
    Double averageTimeToAcknowledge(@Param("since") Instant since);

    @Query("SELECT AVG(i.timeToResolveMinutes) FROM Incident i WHERE i.timeToResolveMinutes IS NOT NULL AND i.createdAt > :since")
    Double averageTimeToResolve(@Param("since") Instant since);

    boolean existsByTriggeredByAnomalyId(UUID anomalyId);

    @Query("SELECT MAX(CAST(SUBSTRING(i.incidentNumber, 5) AS int)) FROM Incident i WHERE i.incidentNumber LIKE 'INC-%'")
    Integer findMaxIncidentNumber();
}
