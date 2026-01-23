package com.smartcharger.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "system_config")
public class SystemConfig extends BaseEntity {

    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(name = "config_value", length = 500)
    private String configValue;

    @Column(name = "description")
    private String description;
}
