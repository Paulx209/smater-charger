package com.smartcharger.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user")
public class User extends BaseEntity {

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone", unique = true, length = 20)
    private String phone;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "warning_threshold")
    private Integer warningThreshold;

    @Column(name = "status", nullable = false)
    private Integer status = 1;
}
