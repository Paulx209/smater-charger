package com.smartcharger.repository;

import com.smartcharger.entity.PriceConfig;
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
 * 费用配置数据访问接口
 */
@Repository
public interface PriceConfigRepository extends JpaRepository<PriceConfig, Long> {

    /**
     * 根据充电桩类型和激活状态查询配置列表（分页）
     */
    Page<PriceConfig> findByChargingPileTypeAndIsActive(String chargingPileType, Integer isActive, Pageable pageable);

    /**
     * 根据充电桩类型查询配置列表（分页）
     */
    Page<PriceConfig> findByChargingPileType(String chargingPileType, Pageable pageable);

    /**
     * 根据激活状态查询配置列表（分页）
     */
    Page<PriceConfig> findByIsActive(Integer isActive, Pageable pageable);

    /**
     * 查询当前有效的费用配置
     * 条件：充电桩类型 + 激活状态 + 当前时间在生效范围内
     * 按创建时间倒序，返回最新的一个
     */
    @Query("SELECT p FROM PriceConfig p WHERE p.chargingPileType = :chargingPileType " +
           "AND p.isActive = 1 " +
           "AND (p.startTime IS NULL OR p.startTime <= :currentTime) " +
           "AND (p.endTime IS NULL OR p.endTime >= :currentTime) " +
           "ORDER BY p.createdTime DESC")
    List<PriceConfig> findCurrentActivePriceConfig(@Param("chargingPileType") String chargingPileType,
                                                    @Param("currentTime") LocalDateTime currentTime);

    /**
     * 检查是否存在冲突的费用配置
     * 冲突条件：同一充电桩类型 + 都是激活状态 + 时间范围有重叠 + 不是当前配置
     */
    @Query("SELECT p FROM PriceConfig p WHERE p.chargingPileType = :chargingPileType " +
           "AND p.isActive = 1 " +
           "AND p.id != :excludeId " +
           "AND (" +
           "  (p.startTime IS NULL AND p.endTime IS NULL) OR " +
           "  (p.startTime IS NULL AND :endTime IS NULL) OR " +
           "  (p.endTime IS NULL AND :startTime IS NULL) OR " +
           "  (:startTime IS NULL AND :endTime IS NULL) OR " +
           "  (p.startTime IS NULL AND p.endTime >= :startTime) OR " +
           "  (p.endTime IS NULL AND p.startTime <= :endTime) OR " +
           "  (:startTime IS NULL AND p.endTime >= :endTime) OR " +
           "  (:endTime IS NULL AND p.startTime <= :startTime) OR " +
           "  (p.startTime <= :endTime AND p.endTime >= :startTime)" +
           ")")
    List<PriceConfig> findConflictingConfigs(@Param("chargingPileType") String chargingPileType,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime,
                                              @Param("excludeId") Long excludeId);
}
