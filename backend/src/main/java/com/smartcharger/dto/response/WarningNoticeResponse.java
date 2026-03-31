package com.smartcharger.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.smartcharger.entity.enums.SendStatus;
import com.smartcharger.entity.enums.WarningNoticeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarningNoticeResponse {

    private Long id;

    private Long userId;

    private String username;

    private String nickname;

    private Long chargingPileId;

    private String pileName;

    private String pileLocation;

    private Long chargingRecordId;

    private WarningNoticeType type;

    private String typeDesc;

    private String content;

    private Integer overtimeMinutes;

    private Integer isRead;

    private SendStatus sendStatus;

    private String sendStatusDesc;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createdTime;
}