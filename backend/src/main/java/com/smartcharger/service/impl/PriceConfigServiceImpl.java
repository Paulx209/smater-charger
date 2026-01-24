package com.smartcharger.service.impl;

import com.smartcharger.common.exception.BusinessException;
import com.smartcharger.common.result.ResultCode;
import com.smartcharger.dto.request.PriceConfigCreateRequest;
import com.smartcharger.dto.request.PriceConfigUpdateRequest;
import com.smartcharger.dto.request.PriceEstimateRequest;
import com.smartcharger.dto.response.PriceConfigResponse;
import com.smartcharger.dto.response.PriceEstimateResponse;
import com.smartcharger.entity.PriceConfig;
import com.smartcharger.repository.PriceConfigRepository;
import com.smartcharger.service.PriceConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 费用配置服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceConfigServiceImpl implements PriceConfigService {

    private final PriceConfigRepository priceConfigRepository;

    @Override
    @Transactional
    public PriceConfigResponse createPriceConfig(PriceConfigCreateRequest request) {
        // 验证时间范围
        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (request.getStartTime().isAfter(request.getEndTime())) {
                throw new BusinessException(ResultCode.INVALID_TIME_RANGE);
            }
        }

        // 检查是否存在冲突配置
        if (request.getIsActive() == 1) {
            List<PriceConfig> conflicts = priceConfigRepository.findConflictingConfigs(
                    request.getChargingPileType(),
                    request.getStartTime(),
                    request.getEndTime(),
                    0L  // 新建时不需要排除任何ID
            );
            if (!conflicts.isEmpty()) {
                throw new BusinessException(ResultCode.PRICE_CONFIG_CONFLICT);
            }
        }

        // 创建配置
        PriceConfig priceConfig = new PriceConfig();
        priceConfig.setChargingPileType(request.getChargingPileType());
        priceConfig.setPricePerKwh(request.getPricePerKwh());
        priceConfig.setServiceFee(request.getServiceFee());
        priceConfig.setStartTime(request.getStartTime());
        priceConfig.setEndTime(request.getEndTime());
        priceConfig.setIsActive(request.getIsActive());

        priceConfig = priceConfigRepository.save(priceConfig);
        log.info("创建费用配置成功: id={}, type={}", priceConfig.getId(), priceConfig.getChargingPileType());

        return convertToResponse(priceConfig);
    }

    @Override
    @Transactional
    public PriceConfigResponse updatePriceConfig(Long id, PriceConfigUpdateRequest request) {
        // 查询配置是否存在
        PriceConfig priceConfig = priceConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.PRICE_CONFIG_NOT_FOUND));

        // 更新字段
        if (request.getPricePerKwh() != null) {
            priceConfig.setPricePerKwh(request.getPricePerKwh());
        }
        if (request.getServiceFee() != null) {
            priceConfig.setServiceFee(request.getServiceFee());
        }
        if (request.getIsActive() != null) {
            // 如果要激活配置，需要检查冲突
            if (request.getIsActive() == 1) {
                List<PriceConfig> conflicts = priceConfigRepository.findConflictingConfigs(
                        priceConfig.getChargingPileType(),
                        priceConfig.getStartTime(),
                        priceConfig.getEndTime(),
                        id
                );
                if (!conflicts.isEmpty()) {
                    throw new BusinessException(ResultCode.PRICE_CONFIG_CONFLICT);
                }
            }
            priceConfig.setIsActive(request.getIsActive());
        }

        priceConfig = priceConfigRepository.save(priceConfig);
        log.info("更新费用配置成功: id={}", id);

        return convertToResponse(priceConfig);
    }

    @Override
    @Transactional
    public void deletePriceConfig(Long id) {
        // 查询配置是否存在
        if (!priceConfigRepository.existsById(id)) {
            throw new BusinessException(ResultCode.PRICE_CONFIG_NOT_FOUND);
        }

        priceConfigRepository.deleteById(id);
        log.info("删除费用配置成功: id={}", id);
    }

    @Override
    public Page<PriceConfigResponse> getPriceConfigList(String chargingPileType, Integer isActive, Integer page, Integer size) {
        // 创建分页对象，按创建时间倒序
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdTime"));

        Page<PriceConfig> priceConfigPage;

        // 根据查询条件查询
        if (chargingPileType != null && isActive != null) {
            priceConfigPage = priceConfigRepository.findByChargingPileTypeAndIsActive(chargingPileType, isActive, pageable);
        } else if (chargingPileType != null) {
            priceConfigPage = priceConfigRepository.findByChargingPileType(chargingPileType, pageable);
        } else if (isActive != null) {
            priceConfigPage = priceConfigRepository.findByIsActive(isActive, pageable);
        } else {
            priceConfigPage = priceConfigRepository.findAll(pageable);
        }

        return priceConfigPage.map(this::convertToResponse);
    }

    @Override
    public PriceConfigResponse getPriceConfigDetail(Long id) {
        PriceConfig priceConfig = priceConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.PRICE_CONFIG_NOT_FOUND));

        return convertToResponse(priceConfig);
    }

    @Override
    public PriceConfigResponse getCurrentPriceConfig(String chargingPileType) {
        LocalDateTime now = LocalDateTime.now();
        List<PriceConfig> configs = priceConfigRepository.findCurrentActivePriceConfig(chargingPileType, now);

        if (configs.isEmpty()) {
            throw new BusinessException(ResultCode.NO_ACTIVE_PRICE_CONFIG);
        }

        // 返回最新创建的一个
        return convertToResponse(configs.get(0));
    }

    @Override
    public PriceEstimateResponse estimatePrice(PriceEstimateRequest request) {
        // 获取当前有效费用配置
        PriceConfigResponse config = getCurrentPriceConfig(request.getChargingPileType());

        // 计算费用（使用 BigDecimal，保留2位小数）
        BigDecimal electricQuantity = request.getElectricQuantity();
        BigDecimal pricePerKwh = config.getPricePerKwh();
        BigDecimal serviceFee = config.getServiceFee();

        // 电费 = 充电量 × 每度电价格
        BigDecimal electricityFee = electricQuantity.multiply(pricePerKwh)
                .setScale(2, RoundingMode.HALF_UP);

        // 服务费总额 = 充电量 × 服务费单价
        BigDecimal serviceFeeTotal = electricQuantity.multiply(serviceFee)
                .setScale(2, RoundingMode.HALF_UP);

        // 总费用 = 电费 + 服务费总额
        BigDecimal totalPrice = electricityFee.add(serviceFeeTotal)
                .setScale(2, RoundingMode.HALF_UP);

        // 构建响应
        PriceEstimateResponse.Breakdown breakdown = PriceEstimateResponse.Breakdown.builder()
                .electricityFee(electricityFee)
                .serviceFee(serviceFeeTotal)
                .build();

        return PriceEstimateResponse.builder()
                .electricQuantity(electricQuantity)
                .pricePerKwh(pricePerKwh)
                .serviceFee(serviceFee)
                .totalPrice(totalPrice)
                .breakdown(breakdown)
                .build();
    }

    @Override
    public BigDecimal calculateFee(String chargingPileType, BigDecimal electricQuantity) {
        // 获取当前有效费用配置
        PriceConfigResponse config = getCurrentPriceConfig(chargingPileType);

        // 计算总费用 = (pricePerKwh + serviceFee) × electricQuantity
        BigDecimal totalFee = config.getPricePerKwh()
                .add(config.getServiceFee())
                .multiply(electricQuantity)
                .setScale(2, RoundingMode.HALF_UP);

        return totalFee;
    }

    /**
     * 转换为响应DTO
     */
    private PriceConfigResponse convertToResponse(PriceConfig priceConfig) {
        return PriceConfigResponse.builder()
                .id(priceConfig.getId())
                .chargingPileType(priceConfig.getChargingPileType())
                .pricePerKwh(priceConfig.getPricePerKwh())
                .serviceFee(priceConfig.getServiceFee())
                .startTime(priceConfig.getStartTime())
                .endTime(priceConfig.getEndTime())
                .isActive(priceConfig.getIsActive())
                .createdTime(priceConfig.getCreatedTime())
                .updatedTime(priceConfig.getUpdatedTime())
                .build();
    }
}
