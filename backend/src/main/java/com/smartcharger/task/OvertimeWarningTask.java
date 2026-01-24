package com.smartcharger.task;

import com.smartcharger.entity.ChargingPile;
import com.smartcharger.entity.ChargingRecord;
import com.smartcharger.entity.SystemConfig;
import com.smartcharger.entity.enums.ChargingPileStatus;
import com.smartcharger.entity.enums.ChargingRecordStatus;
import com.smartcharger.repository.ChargingPileRepository;
import com.smartcharger.repository.ChargingRecordRepository;
import com.smartcharger.repository.SystemConfigRepository;
import com.smartcharger.service.WarningNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 超时占位预警定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OvertimeWarningTask {

    private final ChargingRecordRepository chargingRecordRepository;
    private final ChargingPileRepository chargingPileRepository;
    private final SystemConfigRepository systemConfigRepository;
    private final WarningNoticeService warningNoticeService;

    private static final String CONFIG_KEY_OVERTIME_THRESHOLD = "overtime_warning_threshold";
    private static final Integer DEFAULT_THRESHOLD = 30;

    /**
     * 每5分钟检测一次超时占位情况
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void checkOvertimeCharging() {
        log.info("开始执行超时占位检测任务");

        try {
            // 查询所有状态为"已完成"的充电记录
            List<ChargingRecord> completedRecords = chargingRecordRepository.findAll().stream()
                    .filter(record -> record.getStatus() == ChargingRecordStatus.COMPLETED)
                    .toList();

            log.info("找到{}条已完成的充电记录", completedRecords.size());

            int warningCount = 0;

            for (ChargingRecord record : completedRecords) {
                try {
                    // 获取用户的预警阈值配置
                    Integer threshold = getUserThreshold(record.getUserId());

                    // 计算充电完成时间到当前时间的间隔（分钟）
                    LocalDateTime endTime = record.getEndTime();
                    if (endTime == null) {
                        continue;
                    }

                    Duration duration = Duration.between(endTime, LocalDateTime.now());
                    long minutes = duration.toMinutes();

                    // 如果超过阈值，触发预警
                    if (minutes >= threshold) {
                        // 获取充电桩信息
                        Optional<ChargingPile> pileOpt = chargingPileRepository.findById(record.getChargingPileId());
                        if (pileOpt.isEmpty()) {
                            continue;
                        }

                        ChargingPile pile = pileOpt.get();

                        // 创建预警通知（内部会检查是否已发送过）
                        warningNoticeService.createOvertimeWarning(
                                record.getUserId(),
                                record.getChargingPileId(),
                                record.getId(),
                                pile.getCode(),
                                (int) minutes
                        );

                        // 更新充电桩状态为"超时占位"
                        if (pile.getStatus() != ChargingPileStatus.OVERTIME) {
                            pile.setStatus(ChargingPileStatus.OVERTIME);
                            chargingPileRepository.save(pile);
                        }

                        // 更新充电记录状态为"超时占位"
                        if (record.getStatus() != ChargingRecordStatus.OVERTIME) {
                            record.setStatus(ChargingRecordStatus.OVERTIME);
                            chargingRecordRepository.save(record);
                        }

                        warningCount++;
                    }
                } catch (Exception e) {
                    log.error("处理充电记录超时检测失败: recordId={}, error={}", record.getId(), e.getMessage(), e);
                }
            }

            log.info("超时占位检测任务完成，发送预警通知{}条", warningCount);
        } catch (Exception e) {
            log.error("超时占位检测任务执行失败", e);
        }
    }

    /**
     * 获取用户的预警阈值配置
     */
    private Integer getUserThreshold(Long userId) {
        // 先查询用户级配置
        Optional<SystemConfig> userConfig = systemConfigRepository.findByUserIdAndConfigKey(
                userId, CONFIG_KEY_OVERTIME_THRESHOLD);

        if (userConfig.isPresent()) {
            try {
                return Integer.parseInt(userConfig.get().getConfigValue());
            } catch (NumberFormatException e) {
                log.warn("用户配置值格式错误: userId={}, value={}", userId, userConfig.get().getConfigValue());
            }
        }

        // 如果用户未设置，查询系统级配置
        Optional<SystemConfig> systemConfig = systemConfigRepository.findByUserIdIsNullAndConfigKey(
                CONFIG_KEY_OVERTIME_THRESHOLD);

        if (systemConfig.isPresent()) {
            try {
                return Integer.parseInt(systemConfig.get().getConfigValue());
            } catch (NumberFormatException e) {
                log.warn("系统配置值格式错误: value={}", systemConfig.get().getConfigValue());
            }
        }

        // 返回默认值
        return DEFAULT_THRESHOLD;
    }
}
