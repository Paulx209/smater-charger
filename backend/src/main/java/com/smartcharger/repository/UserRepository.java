package com.smartcharger.repository;

import com.smartcharger.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
