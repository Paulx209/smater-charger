package com.smartcharger.repository;

import com.smartcharger.entity.ChargingPile;
import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.entity.enums.ChargingPileType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 充电桩数据访问接口
 */
@Repository
public interface ChargingPileRepository extends JpaRepository<ChargingPile, Long>, JpaSpecificationExecutor<ChargingPile> {

    /**
     * 根据编号查询充电桩
     */
    ChargingPile findByCode(String code);

    /**
     * 查询附近的充电桩（使用Haversine公式计算距离）
     *
     * @param lng 中心点经度
     * @param lat 中心点纬度
     * @param radius 搜索半径（km）
     * @return 充电桩列表
     */
    @Query(value = "SELECT cp.*, " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(cp.lat)) * " +
            "cos(radians(cp.lng) - radians(:lng)) + sin(radians(:lat)) * " +
            "sin(radians(cp.lat)))) AS distance " +
            "FROM charging_pile cp " +
            "HAVING distance <= :radius " +
            "ORDER BY distance", nativeQuery = true)
    List<ChargingPile> findNearby(@Param("lng") BigDecimal lng,
                                   @Param("lat") BigDecimal lat,
                                   @Param("radius") Double radius);

    /**
     * 根据类型查询充电桩
     */
    List<ChargingPile> findByType(ChargingPileType type);

    /**
     * 根据状态查询充电桩
     */
    List<ChargingPile> findByStatus(ChargingPileStatus status);

    /**
     * 根据编号查询充电桩（排除指定ID）
     */
    @Query("SELECT cp FROM ChargingPile cp WHERE cp.code = :code AND cp.id != :excludeId")
    ChargingPile findByCodeAndIdNot(@Param("code") String code, @Param("excludeId") Long excludeId);

    /**
     * 管理端查询：根据多个条件查询
     */
    @Query("SELECT cp FROM ChargingPile cp WHERE " +
            "(:type IS NULL OR cp.type = :type) AND " +
            "(:status IS NULL OR cp.status = :status) AND " +
            "(:keyword IS NULL OR cp.code LIKE %:keyword% OR cp.location LIKE %:keyword%)")
    Page<ChargingPile> findByAdminConditions(@Param("type") ChargingPileType type,
                                               @Param("status") ChargingPileStatus status,
                                               @Param("keyword") String keyword,
                                               Pageable pageable);

    Long countByStatus(ChargingPileStatus status);
}
