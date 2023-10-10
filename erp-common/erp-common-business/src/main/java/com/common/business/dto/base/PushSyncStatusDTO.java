package com.common.business.dto.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Will
 * @version 1.0
 * @description: 推送状态DTO
 * @date 2023/9/25 16:11
 */
@Data
@NoArgsConstructor
public class PushSyncStatusDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KingdeeDTO {

        /**
         * 业务id
         */
        private String businessId;

        /**
         * 操作类型
         */
        private String operate;

        /**
         * 金蝶id
         */
        private String syncKingdeeId;

        /**
         * 同步状态
         */
        private String syncKingdeeStatus;
    }
}
