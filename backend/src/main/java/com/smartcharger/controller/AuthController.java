package com.smartcharger.controller;

import com.smartcharger.common.result.Result;
import com.smartcharger.dto.request.ChangePasswordRequest;
import com.smartcharger.dto.request.LoginRequest;
import com.smartcharger.dto.request.RegisterRequest;
import com.smartcharger.dto.request.UpdateProfileRequest;
import com.smartcharger.dto.response.LoginResponse;
import com.smartcharger.dto.response.UserInfoResponse;
import com.smartcharger.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        Long userId = getCurrentUserId();
        authService.logout(userId);
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public Result<UserInfoResponse> getCurrentUser() {
        Long userId = getCurrentUserId();
        UserInfoResponse userInfo = authService.getCurrentUser(userId);
        return Result.success(userInfo);
    }

    /**
     * 更新用户资料
     */
    @PutMapping("/profile")
    public Result<UserInfoResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = getCurrentUserId();
        UserInfoResponse userInfo = authService.updateProfile(userId, request);
        return Result.success(userInfo);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = getCurrentUserId();
        authService.changePassword(userId, request);
        return Result.success();
    }

    /**
     * 从SecurityContext中获取当前用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
}
