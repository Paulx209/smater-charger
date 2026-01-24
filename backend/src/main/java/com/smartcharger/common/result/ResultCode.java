package com.smartcharger.common.result;

import lombok.Getter;

/**
 * 返回码枚举
 */
@Getter
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),

    // 业务错误 40xx
    USERNAME_EXISTS(4001, "用户名已存在"),
    PHONE_EXISTS(4002, "手机号已存在"),
    USERNAME_OR_PASSWORD_ERROR(4003, "用户名或密码错误"),
    ACCOUNT_DISABLED(4004, "账号已被禁用"),
    TOKEN_EXPIRED(4005, "Token已过期"),
    TOKEN_INVALID(4006, "Token无效"),
    OLD_PASSWORD_ERROR(4007, "旧密码错误"),
    USER_NOT_FOUND(4008, "用户不存在"),

    // 车辆相关错误 41xx
    LICENSE_PLATE_INVALID(4101, "车牌号格式不正确"),
    LICENSE_PLATE_EXISTS(4102, "车牌号已存在"),
    VEHICLE_IN_USE(4103, "车辆正在使用中，无法删除"),

    // 预约相关错误 42xx
    CHARGING_PILE_NOT_FOUND(4201, "充电桩不存在"),
    CHARGING_PILE_NOT_IDLE(4202, "充电桩不是空闲状态"),
    TIME_CONFLICT(4203, "该时间段已被预约"),
    USER_HAS_PENDING_RESERVATION(4204, "您已有进行中的预约"),
    RESERVATION_CANNOT_CANCEL(4205, "预约状态不允许取消"),

    // 服务器错误 5xx
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),
    SYSTEM_BUSY(504, "系统繁忙，请稍后再试");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
