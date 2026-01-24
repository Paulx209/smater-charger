package com.smartcharger.service;

import com.smartcharger.dto.request.ChangePasswordRequest;
import com.smartcharger.dto.request.LoginRequest;
import com.smartcharger.dto.request.RegisterRequest;
import com.smartcharger.dto.request.UpdateProfileRequest;
import com.smartcharger.dto.response.LoginResponse;
import com.smartcharger.dto.response.UserInfoResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户注册
     */
    void register(RegisterRequest request);

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户登出
     */
    void logout(Long userId);

    /**
     * 获取当前用户信息
     */
    UserInfoResponse getCurrentUser(Long userId);

    /**
     * 更新用户资料
     */
    UserInfoResponse updateProfile(Long userId, UpdateProfileRequest request);

    /**
     * 修改密码
     */
    void changePassword(Long userId, ChangePasswordRequest request);
}
