package com.devops.platform.cost.repository;

import com.devops.platform.cost.model.CloudProvider;
import com.devops.platform.cost.model.CostRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository for CostRecord entities.
 */
@Repository
public interface CostRecordRepository extends JpaRepository<CostRecord, UUID> {

    List<CostRecord> findByResourceIdOrderByRecordDateDesc(UUID resourceId, Pageable pageable);

    List<CostRecord> findByApplicationIdOrderByRecordDateDesc(UUID applicationId, Pageable pageable);

    @Query("SELECT c FROM CostRecord c WHERE c.recordDate BETWEEN :start AND :end ORDER BY c.recordDate")
    List<CostRecord> findByDateRange(
            @Param("start") LocalDate startDate,
            @Param("end") LocalDate endDate);

    @Query("SELECT c.recordDate, SUM(c.costAmount) FROM CostRecord c WHERE c.recordDate BETWEEN :start AND :end GROUP BY c.recordDate ORDER BY c.recordDate")
    List<Object[]> getDailyCosts(
            @Param("start") LocalDate startDate,
            @Param("end") LocalDate endDate);

    @Query("SELECT c.applicationId, SUM(c.costAmount) FROM CostRecord c WHERE c.recordDate BETWEEN :start AND :end GROUP BY c.applicationId ORDER BY SUM(c.costAmount) DESC")
    List<Object[]> getCostByApplicationInRange(
            @Param("start") LocalDate startDate,
            @Param("end") LocalDate endDate);

    @Query("SELECT c.cloudProvider, SUM(c.costAmount) FROM CostRecord c WHERE c.recordDate BETWEEN :start AND :end GROUP BY c.cloudProvider")
    List<Object[]> getCostByProviderInRange(
            @Param("start") LocalDate startDate,
            @Param("end") LocalDate endDate);

    @Query("SELECT c.serviceName, SUM(c.costAmount) FROM CostRecord c WHERE c.recordDate BETWEEN :start AND :end AND c.serviceName IS NOT NULL GROUP BY c.serviceName ORDER BY SUM(c.costAmount) DESC")
    List<Object[]> getCostByServiceInRange(
            @Param("start") LocalDate startDate,
            @Param("end") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT SUM(c.costAmount) FROM CostRecord c WHERE c.recordDate BETWEEN :start AND :end")
    BigDecimal getTotalCostInRange(
            @Param("start") LocalDate startDate,
            @Param("end") LocalDate endDate);
}
