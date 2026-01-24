package com.smartcharger.repository;

import com.smartcharger.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 车辆数据访问接口
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /**
     * 查询用户的所有车辆（按默认车辆优先，创建时间倒序）
     */
    List<Vehicle> findByUserIdOrderByIsDefaultDescCreatedTimeDesc(Long userId);

    /**
     * 查询用户的车辆（带ID，用于权限校验）
     */
    Optional<Vehicle> findByIdAndUserId(Long id, Long userId);

    /**
     * 查询用户的默认车辆
     */
    Optional<Vehicle> findByUserIdAndIsDefault(Long userId, Integer isDefault);

    /**
     * 检查车牌号是否存在（同一用户下）
     */
    boolean existsByUserIdAndLicensePlate(Long userId, String licensePlate);

    /**
     * 检查车牌号是否存在（排除指定ID，用于更新时校验）
     */
    boolean existsByUserIdAndLicensePlateAndIdNot(Long userId, String licensePlate, Long id);

    /**
     * 将用户的所有车辆设置为非默认
     */
    @Modifying
    @Query("UPDATE Vehicle v SET v.isDefault = 0 WHERE v.userId = :userId")
    void clearDefaultByUserId(@Param("userId") Long userId);

    /**
     * 设置指定车辆为默认
     */
    @Modifying
    @Query("UPDATE Vehicle v SET v.isDefault = 1 WHERE v.id = :id")
    void setDefaultById(@Param("id") Long id);

    /**
     * 统计用户的车辆数量
     */
    long countByUserId(Long userId);
}
