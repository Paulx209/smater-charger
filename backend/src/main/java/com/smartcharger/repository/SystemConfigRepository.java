package com.smartcharger.repository;

import com.smartcharger.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 系统配置数据访问接口
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {

    /**
     * 根据用户ID和配置键查询配置
     */
    Optional<SystemConfig> findByUserIdAndConfigKey(Long userId, String configKey);

    /**
     * 根据配置键查询系统级配置（userId为null）
     */
    Optional<SystemConfig> findByUserIdIsNullAndConfigKey(String configKey);
}
