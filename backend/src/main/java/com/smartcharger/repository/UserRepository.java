package com.smartcharger.repository;

import com.smartcharger.entity.User;
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
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查询用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据手机号查询用户
     */
    Optional<User> findByPhone(String phone);

    /**
     * 判断用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 判断手机号是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 根据用户名或手机号查询用户（用于登录）
     */
    Optional<User> findByUsernameOrPhone(String username, String phone);

    /**
     * 管理端：根据多个条件查询用户列表（分页）
     */
    @Query("SELECT u FROM User u WHERE " +
            "(:status IS NULL OR u.status = :status) AND " +
            "(:keyword IS NULL OR u.username LIKE %:keyword% OR u.phone LIKE %:keyword% OR u.nickname LIKE %:keyword%) AND " +
            "(:startDate IS NULL OR u.createdTime >= :startDate) AND " +
            "(:endDate IS NULL OR u.createdTime < :endDate) " +
            "ORDER BY u.createdTime DESC")
    Page<User> findByAdminFilters(@Param("status") Integer status,
                                   @Param("keyword") String keyword,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);

    /**
     * 管理端：查询活跃用户（最近30天有充电记录）
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "INNER JOIN ChargingRecord cr ON u.id = cr.userId " +
            "WHERE cr.startTime >= :thirtyDaysAgo AND " +
            "(:status IS NULL OR u.status = :status) AND " +
            "(:keyword IS NULL OR u.username LIKE %:keyword% OR u.phone LIKE %:keyword% OR u.nickname LIKE %:keyword%) AND " +
            "(:startDate IS NULL OR u.createdTime >= :startDate) AND " +
            "(:endDate IS NULL OR u.createdTime < :endDate) " +
            "ORDER BY u.createdTime DESC")
    Page<User> findActiveUsersByAdminFilters(@Param("status") Integer status,
                                              @Param("keyword") String keyword,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate,
                                              @Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo,
                                              Pageable pageable);

    /**
     * 管理端：根据状态查询用户列表（用于导出）
     */
    @Query("SELECT u FROM User u WHERE " +
            "(:status IS NULL OR u.status = :status) " +
            "ORDER BY u.createdTime DESC")
    List<User> findByStatus(@Param("status") Integer status);

    /**
     * 管理端：查询活跃用户列表（用于导出）
     */
    @Query("SELECT DISTINCT u FROM User u " +
            "INNER JOIN ChargingRecord cr ON u.id = cr.userId " +
            "WHERE cr.startTime >= :thirtyDaysAgo AND " +
            "(:status IS NULL OR u.status = :status) " +
            "ORDER BY u.createdTime DESC")
    List<User> findActiveUsersByStatus(@Param("status") Integer status,
                                        @Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);

    Long countByCreatedTimeGreaterThanEqualAndCreatedTimeLessThan(LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT DATE(u.createdTime), COUNT(u) FROM User u WHERE u.createdTime >= :startTime AND u.createdTime < :endTime GROUP BY DATE(u.createdTime) ORDER BY DATE(u.createdTime)")
    List<Object[]> countDailyNewUsersByCreatedTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
