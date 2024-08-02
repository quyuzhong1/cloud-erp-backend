package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zdy
 * @ClassName DmpTaskMsgDTO
 * @description: TODO
 * @date 2024年07月29日
 * @version: 1.0
 */
@Data
@NoArgsConstructor
public class DmpTaskMsgDTO {
    /**
     * 目标平台
     */
    private String targetPlatformName;
    /**
     * 来源平台
     */
    private String sourcePlatformName;
    /**
     * 消息类型
     */
    private String sourceType;
    /**
     * 数量
     */
    private String total;
}
