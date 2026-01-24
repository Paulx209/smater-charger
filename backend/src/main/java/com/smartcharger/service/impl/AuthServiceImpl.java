package com.smartcharger.service.impl;

import com.smartcharger.common.exception.BusinessException;
import com.smartcharger.common.result.ResultCode;
import com.smartcharger.common.security.JwtTokenProvider;
import com.smartcharger.dto.request.ChangePasswordRequest;
import com.smartcharger.dto.request.LoginRequest;
import com.smartcharger.dto.request.RegisterRequest;
import com.smartcharger.dto.request.UpdateProfileRequest;
import com.smartcharger.dto.response.LoginResponse;
import com.smartcharger.dto.response.UserInfoResponse;
import com.smartcharger.entity.User;
import com.smartcharger.repository.UserRepository;
import com.smartcharger.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        log.info("用户注册: username={}, phone={}", request.getUsername(), request.getPhone());

        // 1. 校验用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 2. 校验手机号是否已存在
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(ResultCode.PHONE_EXISTS);
        }

        // 3. 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setNickname(StringUtils.hasText(request.getNickname()) ?
                request.getNickname() : request.getUsername());
        user.setStatus(1);

        // 4. 保存用户
        userRepository.save(user);

        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录: username={}", request.getUsername());

        // 1. 查询用户（支持用户名或手机号登录）
        User user = userRepository.findByUsernameOrPhone(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR));

        // 2. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        // 3. 检查账号状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 4. 生成JWT Token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        // 5. 构建用户信息
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .name(user.getName())
                .warningThreshold(user.getWarningThreshold())
                .status(user.getStatus())
                .roles(new ArrayList<>())
                .build();

        // 6. 构建登录响应
        LoginResponse response = LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpiration() / 1000)
                .userInfo(userInfo)
                .build();

        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());
        return response;
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        log.info("用户登出: userId={}", userId);

        // 删除Redis中的token
        jwtTokenProvider.deleteToken(userId);

        log.info("用户登出成功: userId={}", userId);
    }

    @Override
    public UserInfoResponse getCurrentUser(Long userId) {
        log.info("获取当前用户信息: userId={}", userId);

        // 查询用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // 构建用户信息
        return UserInfoResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .name(user.getName())
                .warningThreshold(user.getWarningThreshold())
                .status(user.getStatus())
                .roles(new ArrayList<>())
                .build();
    }

    @Override
    @Transactional
    public UserInfoResponse updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("更新用户资料: userId={}", userId);

        // 查询用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // 更新用户信息
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (StringUtils.hasText(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }
        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName());
        }
        if (request.getWarningThreshold() != null) {
            user.setWarningThreshold(request.getWarningThreshold());
        }

        // 保存用户
        userRepository.save(user);

        log.info("更新用户资料成功: userId={}", userId);

        // 返回更新后的用户信息
        return getCurrentUser(userId);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("修改密码: userId={}", userId);

        // 查询用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 删除token，强制重新登录
        jwtTokenProvider.deleteToken(userId);

        log.info("修改密码成功: userId={}", userId);
    }
}
