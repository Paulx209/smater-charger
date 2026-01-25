package com.smartcharger.repository;

import com.smartcharger.entity.FaultReport;
import com.smartcharger.entity.enums.FaultReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 故障报修数据访问接口
 */
@Repository
public interface FaultReportRepository extends JpaRepository<FaultReport, Long> {

    /**
     * 车主端查询：根据用户ID和状态查询
     */
    @Query("SELECT f FROM FaultReport f WHERE f.userId = :userId " +
            "AND (:status IS NULL OR f.status = :status)")
    Page<FaultReport> findByUserIdAndStatus(@Param("userId") Long userId,
                                              @Param("status") FaultReportStatus status,
                                              Pageable pageable);

    /**
     * 管理端查询：根据多个条件查询
     */
    @Query("SELECT f FROM FaultReport f WHERE " +
            "(:chargingPileId IS NULL OR f.chargingPileId = :chargingPileId) AND " +
            "(:status IS NULL OR f.status = :status) AND " +
            "(:startDate IS NULL OR f.createdTime >= :startDate) AND " +
            "(:endDate IS NULL OR f.createdTime <= :endDate)")
    Page<FaultReport> findByConditions(@Param("chargingPileId") Long chargingPileId,
                                         @Param("status") FaultReportStatus status,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         Pageable pageable);

    /**
     * 查询指定充电桩的待处理或处理中的故障报修数量
     */
    @Query("SELECT COUNT(f) FROM FaultReport f WHERE f.chargingPileId = :chargingPileId " +
            "AND f.status IN ('PENDING', 'PROCESSING')")
    Integer countPendingOrProcessingByChargingPileId(@Param("chargingPileId") Long chargingPileId);

    /**
     * 统计指定时间范围内的故障报修数量（按状态分组）
     */
    @Query("SELECT f.status, COUNT(f) FROM FaultReport f WHERE " +
            "(:startDate IS NULL OR f.createdTime >= :startDate) AND " +
            "(:endDate IS NULL OR f.createdTime <= :endDate) " +
            "GROUP BY f.status")
    List<Object[]> countByStatusInDateRange(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * 计算指定时间范围内已修复故障的平均处理时长（分钟）
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, f.createdTime, f.updatedTime)) FROM FaultReport f WHERE " +
            "f.status = 'RESOLVED' AND " +
            "(:startDate IS NULL OR f.createdTime >= :startDate) AND " +
            "(:endDate IS NULL OR f.createdTime <= :endDate)")
    Double calculateAvgHandleTime(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    /**
     * 统计故障最多的充电桩（Top N）
     */
    @Query("SELECT f.chargingPileId, COUNT(f) as faultCount FROM FaultReport f WHERE " +
            "(:startDate IS NULL OR f.createdTime >= :startDate) AND " +
            "(:endDate IS NULL OR f.createdTime <= :endDate) " +
            "GROUP BY f.chargingPileId " +
            "ORDER BY faultCount DESC")
    List<Object[]> findTopFaultPiles(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       Pageable pageable);

    /**
     * 统计充电桩的故障次数
     */
    Long countByChargingPileId(Long chargingPileId);
}
