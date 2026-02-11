package com.smartcharger.repository;

import com.smartcharger.entity.ChargingRecord;
import com.smartcharger.entity.enums.ChargingRecordStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 充电记录数据访问接口
 */
@Repository
public interface ChargingRecordRepository extends JpaRepository<ChargingRecord, Long> {

    /**
     * 查询用户当前充电中的记录
     */
    Optional<ChargingRecord> findByUserIdAndStatus(Long userId, ChargingRecordStatus status);

    /**
     * 查询用户的充电记录（分页）
     */
    Page<ChargingRecord> findByUserId(Long userId, Pageable pageable);

    /**
     * 查询用户指定状态的充电记录（分页）
     */
    Page<ChargingRecord> findByUserIdAndStatus(Long userId, ChargingRecordStatus status, Pageable pageable);

    /**
     * 查询用户指定时间范围的充电记录（分页）
     */
    @Query("SELECT cr FROM ChargingRecord cr WHERE cr.userId = :userId " +
           "AND cr.startTime >= :startDate AND cr.startTime < :endDate " +
           "ORDER BY cr.startTime DESC")
    Page<ChargingRecord> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate,
                                                    Pageable pageable);

    /**
     * 查询用户指定状态和时间范围的充电记录（分页）
     */
    @Query("SELECT cr FROM ChargingRecord cr WHERE cr.userId = :userId " +
           "AND cr.status = :status " +
           "AND cr.startTime >= :startDate AND cr.startTime < :endDate " +
           "ORDER BY cr.startTime DESC")
    Page<ChargingRecord> findByUserIdAndStatusAndDateRange(@Param("userId") Long userId,
                                                             @Param("status") ChargingRecordStatus status,
                                                             @Param("startDate") LocalDateTime startDate,
                                                             @Param("endDate") LocalDateTime endDate,
                                                             Pageable pageable);

    /**
     * 查询用户指定年月的已完成充电记录
     */
    @Query("SELECT cr FROM ChargingRecord cr WHERE cr.userId = :userId " +
           "AND cr.status = 'COMPLETED' " +
           "AND YEAR(cr.startTime) = :year " +
           "AND MONTH(cr.startTime) = :month " +
           "ORDER BY cr.startTime ASC")
    List<ChargingRecord> findCompletedRecordsByMonth(@Param("userId") Long userId,
                                                       @Param("year") Integer year,
                                                       @Param("month") Integer month);

    /**
     * 查询用户指定年份的已完成充电记录
     */
    @Query("SELECT cr FROM ChargingRecord cr WHERE cr.userId = :userId " +
           "AND cr.status = 'COMPLETED' " +
           "AND YEAR(cr.startTime) = :year " +
           "ORDER BY cr.startTime ASC")
    List<ChargingRecord> findCompletedRecordsByYear(@Param("userId") Long userId,
                                                      @Param("year") Integer year);

    /**
     * 管理端：查询所有充电记录（分页）
     */
    @Query("SELECT cr FROM ChargingRecord cr ORDER BY cr.startTime DESC")
    Page<ChargingRecord> findAllRecords(Pageable pageable);

    /**
     * 管理端：根据条件查询充电记录（分页）
     */
    @Query("SELECT cr FROM ChargingRecord cr WHERE " +
           "(:userId IS NULL OR cr.userId = :userId) " +
           "AND (:chargingPileId IS NULL OR cr.chargingPileId = :chargingPileId) " +
           "AND (:status IS NULL OR cr.status = :status) " +
           "AND (:startDate IS NULL OR cr.startTime >= :startDate) " +
           "AND (:endDate IS NULL OR cr.startTime < :endDate) " +
           "ORDER BY cr.startTime DESC")
    Page<ChargingRecord> findByAdminFilters(@Param("userId") Long userId,
                                             @Param("chargingPileId") Long chargingPileId,
                                             @Param("status") ChargingRecordStatus status,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate,
                                             Pageable pageable);

    /**
     * 统计充电桩的充电记录数量
     */
    Long countByChargingPileId(Long chargingPileId);

    /**
     * 统计充电桩的总充电量
     */
    @Query("SELECT SUM(cr.electricQuantity) FROM ChargingRecord cr WHERE cr.chargingPileId = :chargingPileId AND cr.status = 'COMPLETED'")
    java.math.BigDecimal sumElectricQuantityByChargingPileId(@Param("chargingPileId") Long chargingPileId);

    /**
     * 统计充电桩的总营收
     */
    @Query("SELECT SUM(cr.fee) FROM ChargingRecord cr WHERE cr.chargingPileId = :chargingPileId AND cr.status = 'COMPLETED'")
    java.math.BigDecimal sumFeeByChargingPileId(@Param("chargingPileId") Long chargingPileId);

    /**
     * 计算充电桩的平均充电时长（分钟）
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, cr.startTime, cr.endTime)) FROM ChargingRecord cr WHERE cr.chargingPileId = :chargingPileId AND cr.status = 'COMPLETED'")
    Double avgDurationByChargingPileId(@Param("chargingPileId") Long chargingPileId);

    /**
     * 查询充电桩的最后充电时间
     */
    @Query("SELECT MAX(cr.startTime) FROM ChargingRecord cr WHERE cr.chargingPileId = :chargingPileId")
    LocalDateTime findLastChargingTimeByChargingPileId(@Param("chargingPileId") Long chargingPileId);

    /**
     * 统计用户的充电记录数量
     */
    Long countByUserId(Long userId);

    /**
     * 统计用户的总充电量
     */
    @Query("SELECT SUM(cr.electricQuantity) FROM ChargingRecord cr WHERE cr.userId = :userId AND cr.status = 'COMPLETED'")
    java.math.BigDecimal sumElectricQuantityByUserId(@Param("userId") Long userId);

    /**
     * 统计用户的总消费金额
     */
    @Query("SELECT SUM(cr.fee) FROM ChargingRecord cr WHERE cr.userId = :userId AND cr.status = 'COMPLETED'")
    java.math.BigDecimal sumFeeByUserId(@Param("userId") Long userId);

    /**
     * 计算用户的平均充电时长（分钟）
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(MINUTE, cr.startTime, cr.endTime)) FROM ChargingRecord cr WHERE cr.userId = :userId AND cr.status = 'COMPLETED'")
    Double avgDurationByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的最后充电时间
     */
    @Query("SELECT MAX(cr.startTime) FROM ChargingRecord cr WHERE cr.userId = :userId")
    LocalDateTime findLastChargingTimeByUserId(@Param("userId") Long userId);

    /**
     * 查询用户最近N条充电记录
     */
    @Query("SELECT cr FROM ChargingRecord cr WHERE cr.userId = :userId ORDER BY cr.startTime DESC")
    List<ChargingRecord> findRecentRecordsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT cr.chargingPileId) FROM ChargingRecord cr WHERE cr.startTime >= :startTime AND cr.startTime < :endTime")
    Long countUsedChargingPilesByStartTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT SUM(cr.fee) FROM ChargingRecord cr WHERE cr.status = 'COMPLETED' AND cr.startTime >= :startTime AND cr.startTime < :endTime")
    java.math.BigDecimal sumCompletedFeeByStartTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(cr) FROM ChargingRecord cr WHERE cr.status = 'COMPLETED' AND cr.startTime >= :startTime AND cr.startTime < :endTime")
    Long countCompletedRecordsByStartTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT DATE(cr.startTime), SUM(cr.fee), COUNT(cr) FROM ChargingRecord cr WHERE cr.status = 'COMPLETED' AND cr.startTime >= :startTime AND cr.startTime < :endTime GROUP BY DATE(cr.startTime) ORDER BY DATE(cr.startTime)")
    List<Object[]> aggregateDailyRevenueByStartTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(DISTINCT cr.userId) FROM ChargingRecord cr WHERE cr.startTime >= :startTime AND cr.startTime < :endTime")
    Long countDistinctActiveUsersByStartTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT DATE(cr.startTime), COUNT(DISTINCT cr.userId) FROM ChargingRecord cr WHERE cr.startTime >= :startTime AND cr.startTime < :endTime GROUP BY DATE(cr.startTime) ORDER BY DATE(cr.startTime)")
    List<Object[]> countDailyDistinctActiveUsersByStartTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
