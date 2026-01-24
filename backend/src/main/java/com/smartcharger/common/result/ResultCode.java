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

    // 费用配置相关错误 43xx
    PRICE_CONFIG_NOT_FOUND(4301, "费用配置不存在"),
    INVALID_TIME_RANGE(4302, "时间范围无效，开始时间必须早于结束时间"),
    PRICE_CONFIG_CONFLICT(4303, "存在冲突的费用配置，同一充电桩类型在同一时间段只能有一个激活配置"),
    NO_ACTIVE_PRICE_CONFIG(4304, "未找到该充电桩类型的有效费用配置"),

    // 充电记录相关错误 44xx
    CHARGING_RECORD_NOT_FOUND(4401, "充电记录不存在"),
    USER_ALREADY_CHARGING(4402, "您已有正在进行的充电记录"),
    CHARGING_RECORD_NOT_CHARGING(4403, "充电记录不是充电中状态"),
    NO_VALID_RESERVATION(4404, "您没有该充电桩的有效预约"),

    // 预警通知相关错误 45xx
    WARNING_NOTICE_NOT_FOUND(4501, "预警通知不存在"),

    // 系统公告相关错误 46xx
    ANNOUNCEMENT_NOT_FOUND(4601, "公告不存在"),
    ANNOUNCEMENT_NOT_DRAFT(4602, "公告不是草稿状态，无法发布"),
    ANNOUNCEMENT_NOT_PUBLISHED(4603, "公告不是已发布状态，无法下线"),
    ANNOUNCEMENT_NOT_AVAILABLE(4604, "公告不可用或已过期"),

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
