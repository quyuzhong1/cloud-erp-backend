package com.erp.sdk.oms.yunting.cem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 云听CEM凭证DTO
 *
 * @author ERP System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YuntingCredentialDTO {

    /**
     * 来源标识（由云听预设）
     */
    private String source;

    /**
     * 第三方应用ID
     */
    private String thirdPartyId;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 令牌过期时间（秒）
     */
    private Integer expiresIn;

    /**
     * 令牌创建时间戳（毫秒）
     */
    private Long createTimestamp;

    /**
     * 是否已过期
     */
    public boolean isExpired() {
        if (createTimestamp == null || expiresIn == null) {
            return true;
        }
        long currentTime = System.currentTimeMillis();
        long expiryTime = createTimestamp + (expiresIn * 1000L);
        // 提前5分钟认为过期，留有余量
        return currentTime >= (expiryTime - 300000);
    }

    /**
     * 剩余有效时间（秒）
     */
    public long getRemainingTime() {
        if (createTimestamp == null || expiresIn == null) {
            return 0;
        }
        long currentTime = System.currentTimeMillis();
        long expiryTime = createTimestamp + (expiresIn * 1000L);
        long remaining = (expiryTime - currentTime) / 1000;
        return Math.max(0, remaining);
    }
}

