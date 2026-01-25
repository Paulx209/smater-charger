package com.smartcharger.service;

import com.smartcharger.dto.request.BatchUserStatusUpdateRequest;
import com.smartcharger.dto.request.PasswordResetRequest;
import com.smartcharger.dto.request.UserStatusUpdateRequest;
import com.smartcharger.dto.response.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

/**
 * 用户管理服务接口（管理端）
 */
public interface UserManagementService {

    /**
     * 查询用户列表（管理端）
     *
     * @param status    状态筛选（1=启用，0=禁用）
     * @param keyword   关键词搜索（用户名、手机号、昵称）
     * @param startDate 注册开始日期
     * @param endDate   注册结束日期
     * @param isActive  是否活跃用户（最近30天有充电记录）
     * @param page      页码
     * @param size      每页数量
     * @return 用户列表（分页）
     */
    Page<UserAdminResponse> getAdminUserList(Integer status, String keyword,
                                              LocalDate startDate, LocalDate endDate,
                                              Boolean isActive, Integer page, Integer size);

    /**
     * 查询用户详情（管理端）
     *
     * @param id 用户ID
     * @return 用户详情
     */
    UserAdminResponse getAdminUserDetail(Long id);

    /**
     * 更新用户状态
     *
     * @param id      用户ID
     * @param request 更新请求
     * @return 更新后的用户信息
     */
    UserAdminResponse updateUserStatus(Long id, UserStatusUpdateRequest request);

    /**
     * 重置用户密码
     *
     * @param id      用户ID
     * @param request 重置请求
     * @return 重置结果（包含新密码）
     */
    PasswordResetResponse resetUserPassword(Long id, PasswordResetRequest request);

    /**
     * 查看用户充电记录
     *
     * @param id        用户ID
     * @param status    状态筛选
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param page      页码
     * @param size      每页数量
     * @return 充电记录列表（分页）
     */
    Page<ChargingRecordResponse> getUserChargingRecords(Long id, String status,
                                                          LocalDate startDate, LocalDate endDate,
                                                          Integer page, Integer size);

    /**
     * 查看用户预约记录
     *
     * @param id        用户ID
     * @param status    状态筛选
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param page      页码
     * @param size      每页数量
     * @return 预约记录列表（分页）
     */
    Page<ReservationResponse> getUserReservations(Long id, String status,
                                                    LocalDate startDate, LocalDate endDate,
                                                    Integer page, Integer size);

    /**
     * 查看用户违规记录
     *
     * @param id        用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param page      页码
     * @param size      每页数量
     * @return 违规记录列表（分页，包含统计信息）
     */
    Page<ViolationRecordResponse> getUserViolations(Long id, LocalDate startDate, LocalDate endDate,
                                                      Integer page, Integer size);

    /**
     * 导出用户列表
     *
     * @param status   状态筛选
     * @param isActive 是否活跃用户
     * @return Excel 工作簿
     */
    Workbook exportUsers(Integer status, Boolean isActive);

    /**
     * 批量更新用户状态
     *
     * @param request 批量更新请求
     * @return 批量更新结果
     */
    BatchDeleteResultResponse batchUpdateUserStatus(BatchUserStatusUpdateRequest request);
}
