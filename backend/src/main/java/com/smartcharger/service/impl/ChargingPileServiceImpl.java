package com.smartcharger.service.impl;

import com.smartcharger.common.exception.BusinessException;
import com.smartcharger.common.result.ResultCode;
import com.smartcharger.dto.request.ChargingPileQueryRequest;
import com.smartcharger.dto.request.NearbyQueryRequest;
import com.smartcharger.dto.response.ChargingPileResponse;
import com.smartcharger.entity.ChargingPile;
import com.smartcharger.repository.ChargingPileRepository;
import com.smartcharger.service.ChargingPileService;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 充电桩服务实现类
 */
@Slf4j
@Service
public class ChargingPileServiceImpl implements ChargingPileService {

    private final ChargingPileRepository chargingPileRepository;

    // 地球半径（km）
    private static final double EARTH_RADIUS = 6371.0;

    public ChargingPileServiceImpl(ChargingPileRepository chargingPileRepository) {
        this.chargingPileRepository = chargingPileRepository;
    }

    @Override
    public Page<ChargingPileResponse> queryChargingPiles(ChargingPileQueryRequest request) {
        // 构建动态查询条件
        Specification<ChargingPile> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 位置关键词搜索
            if (StringUtils.hasText(request.getKeyword())) {
                predicates.add(criteriaBuilder.like(root.get("location"), "%" + request.getKeyword() + "%"));
            }

            // 类型筛选
            if (request.getType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), request.getType()));
            }

            // 状态筛选
            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 创建分页对象
        Pageable pageable = PageRequest.of(
                request.getPage() - 1,
                request.getSize(),
                Sort.by(Sort.Direction.DESC, "updatedTime")
        );

        // 执行查询
        Page<ChargingPile> page = chargingPileRepository.findAll(specification, pageable);

        // 转换为响应DTO
        return page.map(chargingPile -> {
            ChargingPileResponse response = convertToResponse(chargingPile);

            // 如果传入了经纬度，计算距离
            if (request.getLng() != null && request.getLat() != null) {
                Double distance = calculateDistance(
                        request.getLng(), request.getLat(),
                        chargingPile.getLng(), chargingPile.getLat()
                );
                response.setDistance(distance);
            }

            return response;
        });
    }

    @Override
    public ChargingPileResponse getChargingPileById(Long id) {
        ChargingPile chargingPile = chargingPileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "充电桩不存在"));

        return convertToResponse(chargingPile);
    }

    @Override
    public List<ChargingPileResponse> getNearbyChargingPiles(NearbyQueryRequest request) {
        // 查询附近的充电桩
        List<ChargingPile> nearbyPiles = chargingPileRepository.findNearby(
                request.getLng(),
                request.getLat(),
                request.getRadius()
        );

        // 过滤类型和状态
        List<ChargingPile> filteredPiles = nearbyPiles.stream()
                .filter(pile -> request.getType() == null || pile.getType() == request.getType())
                .filter(pile -> request.getStatus() == null || pile.getStatus() == request.getStatus())
                .collect(Collectors.toList());

        // 转换为响应DTO并计算距离
        return filteredPiles.stream()
                .map(pile -> {
                    ChargingPileResponse response = convertToResponse(pile);
                    Double distance = calculateDistance(
                            request.getLng(), request.getLat(),
                            pile.getLng(), pile.getLat()
                    );
                    response.setDistance(distance);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Double calculateDistance(BigDecimal lng1, BigDecimal lat1, BigDecimal lng2, BigDecimal lat2) {
        if (lng1 == null || lat1 == null || lng2 == null || lat2 == null) {
            return null;
        }

        // 将经纬度转换为弧度
        double lon1 = Math.toRadians(lng1.doubleValue());
        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lon2 = Math.toRadians(lng2.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());

        // Haversine公式
        double dLon = lon2 - lon1;
        double dLat = lat2Rad - lat1Rad;

        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.pow(Math.sin(dLon / 2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 计算距离并保留两位小数
        double distance = EARTH_RADIUS * c;
        return Math.round(distance * 100.0) / 100.0;
    }

    /**
     * 将实体转换为响应DTO
     */
    private ChargingPileResponse convertToResponse(ChargingPile chargingPile) {
        return ChargingPileResponse.builder()
                .id(chargingPile.getId())
                .code(chargingPile.getCode())
                .location(chargingPile.getLocation())
                .lng(chargingPile.getLng())
                .lat(chargingPile.getLat())
                .type(chargingPile.getType())
                .typeDesc(chargingPile.getType().getDescription())
                .power(chargingPile.getPower())
                .status(chargingPile.getStatus())
                .statusDesc(chargingPile.getStatus().getDescription())
                .createdTime(chargingPile.getCreatedTime())
                .updatedTime(chargingPile.getUpdatedTime())
                .build();
    }
}
