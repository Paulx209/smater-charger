package com.smartcharger.controller;

import com.smartcharger.common.result.Result;
import com.smartcharger.dto.request.*;
import com.smartcharger.dto.response.BatchDeleteResultResponse;
import com.smartcharger.dto.response.ChargingPileResponse;
import com.smartcharger.dto.response.ImportResultResponse;
import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.entity.enums.ChargingPileType;
import com.smartcharger.service.ChargingPileAdminService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

/**
 * 充电桩管理控制器（管理端）
 */
@Slf4j
@RestController
@RequestMapping("/admin/charging-piles")
@RequiredArgsConstructor
public class ChargingPileAdminController {

    private final ChargingPileAdminService chargingPileAdminService;

    /**
     * 查询充电桩列表（管理端）
     */
    @GetMapping
    public Result<Page<ChargingPileResponse>> getAdminChargingPileList(
            @RequestParam(required = false) ChargingPileType type,
            @RequestParam(required = false) ChargingPileStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("查询充电桩列表（管理端），参数：type=, status={}, keyword={}, page={}, size={}",
                type, status, keyword, page, size);

        Page<ChargingPileResponse> result = chargingPileAdminService.getAdminChargingPileList(
                type, status, keyword, page, size);
        return Result.success(result);
    }

    /**
     * 查询充电桩详情（管理端）
     */
    @GetMapping("/{id:\\d+}")
    public Result<ChargingPileResponse> getAdminChargingPileDetail(@PathVariable Long id) {
        log.info("查询充电桩详情（管理端），ID：{}", id);

        ChargingPileResponse response = chargingPileAdminService.getAdminChargingPileDetail(id);
        return Result.success(response);
    }

    /**
     * 添加充电桩（管理端）
     */
    @PostMapping
    public Result<ChargingPileResponse> createChargingPile(@Valid @RequestBody ChargingPileCreateRequest request) {
        log.info("添加充电桩（管理端），请求：{}", request);

        ChargingPileResponse response = chargingPileAdminService.createChargingPile(request);
        return Result.success(response);
    }

    /**
     * 更新充电桩信息（管理端）
     */
    @PutMapping("/{id:\\d+}")
    public Result<ChargingPileResponse> updateChargingPile(@PathVariable Long id,
                                                             @Valid @RequestBody ChargingPileUpdateRequest request) {
        log.info("更新充电桩信息（管理端），ID：{}，请求：{}", id, request);

        ChargingPileResponse response = chargingPileAdminService.updateChargingPile(id, request);
        return Result.success(response);
    }

    /**
     * 删除充电桩（管理端）
     */
    @DeleteMapping("/{id:\\d+}")
    public Result<Void> deleteChargingPile(@PathVariable Long id) {
        log.info("删除充电桩（管理端），ID：{}", id);

        chargingPileAdminService.deleteChargingPile(id);
        return Result.success(null);
    }

    /**
     * 手动更新充电桩状态（管理端）
     */
    @PutMapping("/{id:\\d+}/status")
    public Result<ChargingPileResponse> updateChargingPileStatus(@PathVariable Long id,
                                                                   @Valid @RequestBody ChargingPileStatusUpdateRequest request) {
        log.info("更新充电桩状态（管理端），ID：{}，请求：{}", id, request);

        ChargingPileResponse response = chargingPileAdminService.updateChargingPileStatus(id, request);
        return Result.success(response);
    }

    /**
     * 批量删除充电桩（管理端）
     */
    @DeleteMapping("/batch")
    public Result<BatchDeleteResultResponse> batchDeleteChargingPiles(@Valid @RequestBody ChargingPileBatchDeleteRequest request) {
        log.info("批量删除充电桩（管理端），请求：{}", request);

        BatchDeleteResultResponse response = chargingPileAdminService.batchDeleteChargingPiles(request);
        return Result.success(response);
    }

    /**
     * 批量导入充电桩（管理端）
     */
    @PostMapping("/import")
    public Result<ImportResultResponse> importChargingPiles(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("批量导入充电桩（管理端），文件名：{}", file.getOriginalFilename());

        ImportResultResponse response = chargingPileAdminService.importChargingPiles(file);
        return Result.success(response);
    }

    /**
     * 批量导出充电桩（管理端）
     */
    @GetMapping("/export")
    public void exportChargingPiles(
            @RequestParam(required = false) ChargingPileType type,
            @RequestParam(required = false) ChargingPileStatus status,
            HttpServletResponse response) throws IOException {
        log.info("批量导出充电桩（管理端），参数：type={}, status={}", type, status);

        Workbook workbook = chargingPileAdminService.exportChargingPiles(type, status);

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=charging_piles_" + LocalDate.now() + ".xlsx");

        // 写入响应流
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
